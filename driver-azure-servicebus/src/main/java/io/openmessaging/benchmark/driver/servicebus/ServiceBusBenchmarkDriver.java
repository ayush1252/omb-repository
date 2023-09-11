/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openmessaging.benchmark.driver.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.credential.adapter.CredentialProvider;
import io.openmessaging.benchmark.driver.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServiceBusBenchmarkDriver implements BenchmarkDriver {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusBenchmarkDriver.class);
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    protected final List<BenchmarkProducer> producers = Collections.synchronizedList(new ArrayList<>());
    private final List<BenchmarkConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    protected String namespace;
    protected Properties producerProperties;
    protected ConfigProvider configProvider;
    protected DriverConfiguration driverConfiguration;
    private String topicPrefix;
    private ServiceBusAdministrator serviceBusAdministrator;

    @Override
    public void initialize(File configurationFile, org.apache.bookkeeper.stats.StatsLogger statsLogger) throws IOException {
        configProvider = ConfigProvider.getInstance();
        CredentialProvider credentialProvider = CredentialProvider.getInstance();

        driverConfiguration = mapper.readValue(configurationFile, DriverConfiguration.class);
        log.info("Initializing " + this.getClass().getSimpleName() + " with configuration " + driverConfiguration.name);
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
        serviceBusAdministrator = new ServiceBusAdministrator(driverConfiguration.namespaceMetadata);

        if (driverConfiguration.reset) {
            String resourceGroup = driverConfiguration.namespaceMetadata.ResourceGroup;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    serviceBusAdministrator.deleteExistingEntities(resourceGroup, namespace);
                }
            }).start();
        }

        if (driverConfiguration.namespaceMetadata.SASKeyValue == null) {
            driverConfiguration.namespaceMetadata.SASKeyValue = credentialProvider.getCredential(driverConfiguration.namespaceMetadata.NamespaceName + "-SASKey");
        }
    }

    @Override
    public String getTopicNamePrefix() {
        return topicPrefix;
    }

    @Override
    public CompletableFuture<Void> createTopic(String topic, int partitions) {
        return CompletableFuture.runAsync(() -> serviceBusAdministrator.createTopic(topic, partitions));
    }

    @Override
    public CompletableFuture<Void> notifyTopicCreation(String topic, int partitions) {
        // NO-OP
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<BenchmarkProducer> createProducer(String topic) {
        ServiceBusSenderAsyncClient sbProducerClient = new ServiceBusClientBuilder()
                .connectionString(createConnectionString(driverConfiguration.namespaceMetadata.NamespaceName,
                        configProvider.getConfigurationValue(ConfigurationKey.FQDNSuffix), driverConfiguration.namespaceMetadata.SASKeyName, driverConfiguration.namespaceMetadata.SASKeyValue))
                .sender()
                .queueName(topic)
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
    public CompletableFuture<BenchmarkConsumer> createConsumer(String topic, String subscriptionName, Optional<Integer> partition, ConsumerCallback consumerCallback) {
        ServiceBusProcessorClient sbProcessorClient = new ServiceBusClientBuilder()
                .connectionString(createConnectionString(driverConfiguration.namespaceMetadata.NamespaceName,
                        configProvider.getConfigurationValue(ConfigurationKey.FQDNSuffix), driverConfiguration.namespaceMetadata.SASKeyName, driverConfiguration.namespaceMetadata.SASKeyValue))
                .processor()
                .queueName(topic)
                .processMessage(context -> ServiceBusBenchmarkConsumer.processEvent(context, consumerCallback))
                .processError(errorContext -> log.error("exception occur while consuming message " + errorContext.toString()))
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

    private String createConnectionString(String namespaceName, String domainName, String sasKeyName, String sasKeyValue) {
        return new ConnectionStringBuilder()
                .setEndpoint(namespaceName, StringUtils.stripStart(domainName, "."))
                .setSasKeyName(sasKeyName)
                .setSasKey(sasKeyValue)
                .toString();
    }
}
