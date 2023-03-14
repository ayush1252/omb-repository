/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.openmessaging.benchmark.driver.eventhubs.syncproducer;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import io.openmessaging.benchmark.driver.eventhubs.EventHubsBenchmarkDriver;
import org.apache.bookkeeper.stats.StatsLogger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class EventHubsBenchmarkSyncProducerDriver extends EventHubsBenchmarkDriver {

    @Override
    public void initialize(File configurationFile, StatsLogger statsLogger) throws IOException {
        super.initialize(configurationFile, statsLogger);
    }

    @Override
    public CompletableFuture<BenchmarkProducer> createProducer(String topic) {
        EventHubProducerClient ehProducerClient = new EventHubClientBuilder()
                .credential(namespace + configProvider.getConfigurationValue(ConfigurationKey.FQDNSuffix), topic, credential)
                .buildProducerClient();
        BenchmarkProducer benchmarkProducer = new EventHubsBenchmarkSyncProducer(ehProducerClient, producerProperties);
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
}
