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
package io.openmessaging.benchmark.driver.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import com.azure.resourcemanager.eventhubs.models.EventHub;

import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.appconfig.adapter.EnvironmentName;
import io.openmessaging.benchmark.appconfig.adapter.NamespaceMetadata;
import io.openmessaging.benchmark.driver.BenchmarkConsumer;
import io.openmessaging.benchmark.driver.BenchmarkDriver;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import io.openmessaging.benchmark.driver.ConsumerCallback;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHubsBenchmarkDriver implements BenchmarkDriver {

    private static final Logger log = LoggerFactory.getLogger(EventHubsBenchmarkDriver.class);

    private String topicPrefix;
    private String namespace;
    private TokenCredential credential;

    private final List<BenchmarkProducer> producers = Collections.synchronizedList(new ArrayList<>());
    private final List<BenchmarkConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    private BlobContainerAsyncClient blobContainerAsyncClient;
    private EventHubAdministrator eventHubAdministrator;
    private ConfigProvider configProvider;

    @Override
    public void initialize(File configurationFile, org.apache.bookkeeper.stats.StatsLogger statsLogger) throws IOException {
        configProvider = ConfigProvider.getInstance(EnvironmentName.Production.toString());
        NamespaceMetadata metadata = configProvider
                .getNamespaceMetaData(StringUtils.split(configurationFile.getName(), '.')[0]);
        Config config = mapper.readValue(configurationFile, Config.class);

        Properties commonProperties = new Properties();
        commonProperties.load(new StringReader(config.commonConfig));

        Properties producerProperties = new Properties();
        producerProperties.putAll(commonProperties);
        producerProperties.load(new StringReader(config.producerConfig));

        Properties consumerProperties = new Properties();
        consumerProperties.putAll(commonProperties);
        consumerProperties.load(new StringReader(config.consumerConfig));

        Properties topicProperties = new Properties();
        topicProperties.load(new StringReader(config.topicConfig));

        topicPrefix = topicProperties.getProperty("topic.name.prefix");
        namespace = metadata.NamespaceName;

        blobContainerAsyncClient = CreateCheckpointStore(consumerProperties);
        eventHubAdministrator = new EventHubAdministrator(metadata);

        if (config.reset) {
            String resourceGroup = metadata.ResourceGroup;

            for (EventHub eh : eventHubAdministrator.getManager().namespaces().eventHubs().listByNamespace(resourceGroup, namespace)) {
                eventHubAdministrator.getManager().namespaces().eventHubs().deleteByName(resourceGroup, namespace, eh.name());
            }
        }

        credential = new DefaultAzureCredentialBuilder().build();
    }

    @Override
    public String getTopicNamePrefix() {
        return topicPrefix;
    }

    @Override
    public CompletableFuture<Void> createTopic(String topic, int partitions) {
        return CompletableFuture.runAsync(() -> eventHubAdministrator.createTopic(topic, partitions));
    }

    @Override
    public CompletableFuture<Void> notifyTopicCreation(String topic, int partitions) {
        //NO-OP
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<BenchmarkProducer> createProducer(String topic) {


        EventHubProducerClient ehProducerClient = new EventHubClientBuilder()
                .credential(namespace + configProvider.getConfigurationValue(ConfigurationKey.FQDNSuffix), topic, credential)
                .buildProducerClient();
        BenchmarkProducer benchmarkProducer = new EventHubsBenchmarkProducer(ehProducerClient);
        try {
            producers.add(benchmarkProducer);
            return CompletableFuture.completedFuture(benchmarkProducer);
        } catch (Exception e) {
            ehProducerClient.close();
            CompletableFuture<BenchmarkProducer> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<BenchmarkConsumer> createConsumer(String topic, String subscriptionName,
                                                               Optional<Integer> partition,
                                                               ConsumerCallback consumerCallback) {

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .credential(namespace + configProvider.getConfigurationValue(ConfigurationKey.FQDNSuffix), topic, credential)
                .consumerGroup(subscriptionName)
                .processEvent(eventContext -> EventHubsBenchmarkConsumer.processEvent(eventContext, consumerCallback))
                .processError(errorContext -> log.error("exception occur while consuming message " +  errorContext.getThrowable().getMessage()))
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient))
                .buildEventProcessorClient();

        try {
            BenchmarkConsumer benchmarkConsumer = new EventHubsBenchmarkConsumer(eventProcessorClient);
            consumers.add(benchmarkConsumer);
            return CompletableFuture.completedFuture(benchmarkConsumer);
        } catch (Throwable t) {
            eventProcessorClient.stop();
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

    private BlobContainerAsyncClient CreateCheckpointStore(Properties consumerProperties) {
        // Construct the blob container endpoint from the arguments.
        String containerEndpoint = String.format("https://%s.blob.core.windows.net/%s",
                configProvider.getConfigurationValue(ConfigurationKey.StorageAccountName),
                configProvider.getConfigurationValue(ConfigurationKey.StorageContainerName));

        return  new BlobContainerClientBuilder()
                .endpoint(containerEndpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE))
                .buildAsyncClient();
    }

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
