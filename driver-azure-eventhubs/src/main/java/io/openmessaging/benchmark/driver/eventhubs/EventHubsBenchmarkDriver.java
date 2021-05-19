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

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;

import com.microsoft.azure.eventhubs.EventHubException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventHubsBenchmarkDriver implements BenchmarkDriver {

    private String connectionString;
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String subscriptionId;

    private String namespace;
    private String namespaceResourceId;
    private String sasKeyName;
    private String sasKey;



    private String topicName;
    private String storageConnectionString;
    private String storageContainerName;

    private Config config;
    private List<BenchmarkProducer> producers = Collections.synchronizedList(new ArrayList<>());
    private List<BenchmarkConsumer> consumers = Collections.synchronizedList(new ArrayList<>());
    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    private Properties topicProperties;
    private Properties producerProperties;
    private Properties consumerProperties;

    private EventHubsManager eventHubsManager;
    private BlobContainerAsyncClient blobContainerAsyncClient;

    @Override
    public void initialize(File configurationFile, org.apache.bookkeeper.stats.StatsLogger statsLogger) throws IOException {
        config = mapper.readValue(configurationFile, Config.class);
        Properties commonProperties = new Properties();
        commonProperties.load(new StringReader(config.commonConfig));

        producerProperties = new Properties();
        commonProperties.forEach((key, value) -> producerProperties.put(key, value));

        consumerProperties = new Properties();
        commonProperties.forEach((key, value) -> consumerProperties.put(key, value));
        consumerProperties.load(new StringReader(config.consumerConfig));

        topicProperties = new Properties();
        topicProperties.load(new StringReader(config.topicConfig));

        connectionString = commonProperties.getProperty("connection.string");
        clientId = commonProperties.getProperty("client.id");
        clientSecret = commonProperties.getProperty("client.secret");
        tenantId = commonProperties.getProperty("tenant.id");
        subscriptionId = commonProperties.getProperty("subscription.id");

        namespace = commonProperties.getProperty("namespace");
        namespaceResourceId = commonProperties.getProperty("namespace.id");

        sasKeyName = commonProperties.getProperty("sas.key.name");
        sasKey = commonProperties.getProperty("sas.key");

        storageConnectionString = consumerProperties.getProperty("storage.connection.string");
        storageContainerName = consumerProperties.getProperty("storage.container.name");

        topicName = topicProperties.getProperty("topic.name.prefix");

        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
        eventHubsManager = EventHubsManager.configure()
                .authenticate(clientSecretCredential, profile);

        // Checkpoint Store
        blobContainerAsyncClient = new BlobContainerClientBuilder()
                .connectionString(storageConnectionString)
                .containerName(storageContainerName)
                .buildAsyncClient();

        System.out.println("Updated Patch 2==================================================");
        // ToDo : Cleanup after each test iteration

    }

    @Override
    public String getTopicNamePrefix() {
        return topicName;
    }

    @Override
    public CompletableFuture<Void> createTopic(String topic, int partitions) {

        return CompletableFuture.runAsync(() -> {
            eventHubsManager.namespaces()
                    .eventHubs()
                    .define(topic)
                    .withExistingNamespaceId(namespaceResourceId)
                    .withPartitionCount(partitions)
                    .create();
        });
    }

    @Override
    public CompletableFuture<Void> notifyTopicCreation(String topic, int partitions) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<BenchmarkProducer> createProducer(String topic) {
        final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName(namespace)
                .setEventHubName(topic)
                .setSasKeyName(sasKeyName)
                .setSasKey(sasKey);

        EventHubClient ehClient = null;
        try {
            ehClient = EventHubClient.createSync(connStr.toString(), executorService);
            BenchmarkProducer benchmarkProducer = new EventHubsBenchmarkProducer(ehClient);
            producers.add(benchmarkProducer);

            return CompletableFuture.completedFuture(benchmarkProducer);
        } catch (Exception e){
            ehClient.close();
            CompletableFuture<BenchmarkProducer> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<BenchmarkConsumer> createConsumer(String topic, String subscriptionName,
                                                               Optional<Integer> partition,
                                                               ConsumerCallback consumerCallback) {
        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
                .connectionString(connectionString, topic)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .processEvent(eventContext -> {
                    // TODO Verify time stamp
                    consumerCallback.messageReceived(eventContext.getEventData().getBody(),
                            TimeUnit.MILLISECONDS.toNanos(
                                    eventContext.getEventData().getEnqueuedTime().toEpochMilli()));
                })
                .processError(errorContext -> {
                    // ToDo
                })
                .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));
        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();

        try {
            BenchmarkConsumer benchmarkConsumer = new EventHubsBenchmarkConsumer(eventProcessorClient, consumerCallback);
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


    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
