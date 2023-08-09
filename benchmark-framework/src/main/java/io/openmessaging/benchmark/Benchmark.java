/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.openmessaging.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.driver.DriverConfiguration;
import io.openmessaging.benchmark.driver.NamespaceMetadata;
import io.openmessaging.benchmark.kusto.adapter.KustoAdapter;
import io.openmessaging.benchmark.output.Metadata;
import io.openmessaging.benchmark.output.TestResult;
import io.openmessaging.benchmark.worker.HTTPWorkerClient;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.openmessaging.benchmark.worker.DistributedWorkersEnsemble;
import io.openmessaging.benchmark.worker.LocalWorker;
import io.openmessaging.benchmark.worker.Worker;

import static java.util.stream.Collectors.toList;

public class Benchmark {

    static ConfigProvider provider;
    static KustoAdapter adapter;

    static {
        try {
            //Ensure that you have set EnvironmentVariable AppConfigConnectionString before calling this
            provider = ConfigProvider.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
        final Arguments arguments = new Arguments();
        JCommander jc = new JCommander(arguments);
        jc.setProgramName("messaging-benchmark");

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(-1);
        }

        if (arguments.help) {
            jc.usage();
            System.exit(-1);
        }

        try{
            executeBenchmarkingRun(arguments);
        } catch(Exception e){
           log.error(e.toString());
        } finally{
            System.exit(0);
        }
    }

    public static void executeBenchmarkingRun(Arguments arguments) throws Exception {
        if (arguments.workers != null && arguments.workersFile != null) {
            System.err.println("Only one between --workers and --workers-file can be specified");
            throw new RuntimeException("Conflict between worker roles");
        }

        if (arguments.workers == null && arguments.workersFile == null) {
            File defaultFile = new File("workers.yaml");
            if (defaultFile.exists()) {
                log.info("Using default worker file workers.yaml");
                arguments.workersFile = defaultFile;
            }
        }

        if (arguments.workersFile != null) {
            log.info("Reading workers list from {}", arguments.workersFile);
            arguments.workers = mapper.readValue(arguments.workersFile, Workers.class).workers;
        }

        // Dump configuration variables
        log.info("Starting benchmark with config: {}", writer.writeValueAsString(arguments));

        Map<String, Workload> workloads = new TreeMap<>();
        for (String path : arguments.workloads) {
            File file = new File(path);
            String name = file.getName().substring(0, file.getName().lastIndexOf('.'));

            workloads.put(name, mapper.readValue(file, Workload.class));
        }

        log.info("Workloads: {}", writer.writeValueAsString(workloads));

        Worker worker;

        if (arguments.workers != null && !arguments.workers.isEmpty()) {
            List<Worker> workers =
                    arguments.workers.stream().map(HTTPWorkerClient::new).collect(toList());
            worker = new DistributedWorkersEnsemble(workers, arguments.producerWorkers);
        } else {
            // Use local worker implementation
            worker = new LocalWorker();
        }

        workloads.forEach((workloadName, workload) -> {

            try {
                workload.validate();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw e;
            }

            arguments.drivers.forEach(driverConfig -> {
                try {
                    File driverConfigFile = new File(driverConfig);
                    DriverConfiguration driverConfiguration = mapper.readValue(driverConfigFile,
                            DriverConfiguration.class);

                    if (driverConfiguration.namespaceMetadata == null) {
                        String metadataString = arguments.namespaceMetadata != null
                                ? arguments.namespaceMetadata
                                : provider.getNamespaceMetaData(driverConfiguration.identifier);

                        if (metadataString != null) {
                            try {
                                driverConfiguration.namespaceMetadata = new ObjectMapper().readValue(metadataString, NamespaceMetadata.class);
                            } catch (Exception e) {
                                log.warn("Failed to parse NamespaceMetadata. Unable to deserialize metadata string.");
                            }
                        }
                    }
                    if (driverConfiguration.namespaceMetadata == null) {
                        throw new RuntimeException("No Namespace Information Provided For the Test. Breaking");
                    }

                    log.info("--------------- WORKLOAD : {} --- DRIVER : {}---------------", workload.name,
                            driverConfiguration.name);
                    UUID uniqueRunId = UUID.randomUUID();
                    // Stop any left over workload
                    worker.stopAll();
                    File tempFile = File.createTempFile("driver-configuration" + System.currentTimeMillis(), ".tmp");
                    mapper.writeValue(tempFile, driverConfiguration);
                    worker.initializeDriver(tempFile);

                    WorkloadGenerator generator = new WorkloadGenerator(driverConfiguration.name, workload, worker, uniqueRunId);

                    TestResult result = generator.run();

                    //Enriching Metadata
                    result.testDetails.product = driverConfiguration.product;
                    result.testDetails.sku = driverConfiguration.sku;
                    result.testDetails.protocol = driverConfiguration.protocol;

                    //Fetch BatchSize in KB and BatchCount
                    Properties producerProperties = new Properties();
                    producerProperties.load(new StringReader(driverConfiguration.producerConfig));

                    String batchSize = Optional.ofNullable(producerProperties.getProperty("batch.size"))
                            .orElse("1048576");
                    batchSize = (Integer.parseInt(batchSize) / 1024) + "KB";
                    int batchCount = Integer.parseInt(
                            Optional.ofNullable(producerProperties.getProperty("batch.count"))
                                    .orElse("1"));

                    result.testDetails.metadata = Metadata.builder()
                            .workload(arguments.output.split("-")[0]) //Replacing workload name with test name
                            .payload(workload.payloadFile)
                            .namespaceName(driverConfiguration.namespaceMetadata.NamespaceName)
                            .topics(workload.topics)
                            .partitions(workload.partitionsPerTopic)
                            .producerCount(workload.producersPerTopic)
                            .consumerGroups(workload.subscriptionsPerTopic)
                            .consumerCount(workload.consumerPerSubscription * workload.subscriptionsPerTopic)
                            .batchCount(batchCount)
                            .batchSize(batchSize)
                            .tags(Optional.ofNullable(arguments.tags).orElse(new ArrayList<>())
                                    .stream()
                                    .map(s-> CaseUtils.toCamelCase(s, true))
                                    .collect(toList()))
                            .build();

                    String fileNamePrefix = arguments.output.length() > 0 ? arguments.output
                            : String.format("%s-%s-%s", workloadName, driverConfiguration.name,
                            dateFormat.format(new Date()));

                    WriteTestResults(fileNamePrefix, result);
                    if(arguments.visualizeUsingKusto){
                        if(adapter == null){
                            adapter = new KustoAdapter(provider.getConfigurationValue(ConfigurationKey.KustoEndpoint),
                                    provider.getConfigurationValue(ConfigurationKey.KustoDatabaseName));
                        }
                        adapter.uploadDataToKustoCluster(fileNamePrefix);
                    }
                    log.info("Completed Execution of Run");
                    generator.close();
                } catch (Exception e) {
                    log.error("Failed to run the workload '{}' for driver '{}'", workload.name, driverConfig, e);
                    throw new RuntimeException(e);
                } finally {
                    try {
                        worker.stopAll();
                    } catch (IOException ignored) {
                    }
                }
            });
        });
        worker.close();
        log.info("End of Benchmarking Run");
    }


    private static void WriteTestResults(String fileNamePrefix, TestResult result) throws IOException {
        writer.writeValue(new File(fileNamePrefix + "-details.json"), result.testDetails);
        writer.writeValue(new File(fileNamePrefix + "-snapshot.json"), result.snapshotResultList);
        writer.writeValue(new File(fileNamePrefix + "-aggregate.json"), result.aggregateResult);
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static {
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    private static final ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private static final Logger log = LoggerFactory.getLogger(Benchmark.class);
}
