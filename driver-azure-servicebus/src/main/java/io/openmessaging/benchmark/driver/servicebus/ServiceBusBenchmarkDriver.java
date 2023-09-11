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
package io.openmessaging.benchmark.driver.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import com.azure.storage.blob.BlobContainerAsyncClient;

import com.azure.resourcemanager.eventhubs.models.EventHub;

import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import io.openmessaging.benchmark.driver.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import io.openmessaging.benchmark.storage.adapter.StorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBusBenchmarkDriver implements BenchmarkDriver {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusBenchmarkDriver.class);

    private String topicPrefix;
    protected String namespace;
    protected TokenCredential credential;
    protected Properties producerProperties;

    protected final List<BenchmarkProducer> producers = Collections.synchronizedList(new ArrayList<>());
    private final List<BenchmarkConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    private BlobContainerAsyncClient blobContainerAsyncClient;
    private ServiceBusAdministrator serviceBusAdministrator;
    protected ConfigProvider configProvider;

    @Override
    public void initialize(File configurationFile, org.apache.bookkeeper.stats.StatsLogger statsLogger) throws IOException {
        configProvider = ConfigProvider.getInstance();
        CredentialProvider credentialProvider = CredentialProvider.getInstance();

        DriverConfiguration driverConfiguration = mapper.readValue(configurationFile, DriverConfiguration.class);
        log.info("Initializing "+ this.getClass().getSimpleName() + " with configuration " +  driverConfiguration.name);
        log.info("Using Namespace for this test run- " + driverConfiguration.namespaceMetadata.NamespaceName);

        Properties commonProperties = new Properties();
        commonProperties.load(new StringReader(driverConfiguration.commonConfig));

        producerProperties = new Properties();
        producerProperties.putAll(commonProperties);
        producerProperties.load(new StringReader(driverConfiguration.producerConfig));
        producerProperties.putIfAbsent("batch.size", "1048576");
        producerProperties.putIfAbsent("batch.count", "1");

        Properties topicProperties = new Properties();
        topicProperties.load(new StringReader(driverConfiguration.topicConfig));

        topicPrefix = topicProperties.getProperty("topic.name.prefix");
        namespace = driverConfiguration.namespaceMetadata.NamespaceName;

        blobContainerAsyncClient = StorageAdapter.GetAsyncStorageClient(configProvider.getConfigurationValue(ConfigurationKey.StorageAccountName),
                configProvider.getConfigurationValue(ConfigurationKey.StorageContainerName));
        serviceBusAdministrator = new ServiceBusAdministrator(driverConfiguration.namespaceMetadata);

//        if (driverConfiguration.reset) {
//            String resourceGroup = driverConfiguration.namespaceMetadata.ResourceGroup;
//            log.info("Deleting existing entities");
//            for (EventHub eh : serviceBusAdministrator.getManager().namespaces().eventHubs().listByNamespace(resourceGroup, namespace)) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        serviceBusAdministrator.getManager().namespaces().eventHubs().deleteByName(resourceGroup, namespace, eh.name());
//                        log.info("Successfully deleted entity "+ eh.name());
//                    }
//                }).start();
//            }
//        }

        if(driverConfiguration.namespaceMetadata.SASKeyValue == null) {
            driverConfiguration.namespaceMetadata.SASKeyValue = credentialProvider.getCredential(driverConfiguration.namespaceMetadata.NamespaceName+"-SASKey");
        }

        credential =  new EventHubSharedKeyCredential(driverConfiguration.namespaceMetadata.SASKeyName, driverConfiguration.namespaceMetadata.SASKeyValue);
    }

    @Override
    public String getTopicNamePrefix() {
        return topicPrefix;
    }

    @Override
    public CompletableFuture<Void> createTopic(String topic, int partitions) {
        //NO-OP
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> notifyTopicCreation(String topic, int partitions) {
        //NO-OP
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<BenchmarkProducer> createProducer(String topic) {
        ServiceBusSenderAsyncClient sbProducerClient = new ServiceBusClientBuilder()
                .connectionString("<My Connection String>")
                .sender()
                .queueName("<My Queue name")
                .buildAsyncClient();
        BenchmarkProducer benchmarkProducer = new ServiceBusBenchmarkProducer(sbProducerClient, producerProperties);
        try {
            producers.add(benchmarkProducer);
            return CompletableFuture.completedFuture(benchmarkProducer);
        } catch (Exception e) {
            sbProducerClient.close();
            CompletableFuture<BenchmarkProducer> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<BenchmarkConsumer> createConsumer(String topic, String subscriptionName,
                                                               Optional<Integer> partition,
                                                               ConsumerCallback consumerCallback) {

        serviceBusAdministrator.createConsumerGroupIfNotPresent(topic, subscriptionName);

        ServiceBusProcessorClient sbProcessorClient = new ServiceBusClientBuilder()
                .connectionString("My Connection String")
                .processor()
                .queueName("My Queue Name")
                //..processMessage(eventContext -> ServiceBusBenchmarkConsumer.processEvent(eventContext, consumerCallback))
                .processMessage(context -> ServiceBusBenchmarkConsumer.processEvent(context, consumerCallback))
                .processError(errorContext -> log.error("exception occur while consuming message " +  errorContext.toString()))
                .buildProcessorClient();
        try {
            BenchmarkConsumer benchmarkConsumer = new ServiceBusBenchmarkConsumer(sbProcessorClient);
            consumers.add(benchmarkConsumer);
            return CompletableFuture.completedFuture(benchmarkConsumer);
        } catch (Throwable t) {
            sbProcessorClient.stop();
            CompletableFuture<BenchmarkConsumer> future = new CompletableFuture<>();
            future.completeExceptionally(t);
            return future;
        }
    }


    @Override
    public void close() throws Exception {
        for (BenchmarkProducer producer : producers) {
            producer.close();
        }

        for (BenchmarkConsumer consumer : consumers) {
            consumer.close();
        }
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
