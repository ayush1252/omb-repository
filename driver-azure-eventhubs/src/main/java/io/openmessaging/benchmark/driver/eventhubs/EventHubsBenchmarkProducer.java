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

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.*;

public class EventHubsBenchmarkProducer implements BenchmarkProducer {
    private static final Logger log = LoggerFactory.getLogger(EventHubsBenchmarkProducer.class);
    final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);

    private final EventHubProducerClient producerClient;

    public EventHubsBenchmarkProducer(EventHubProducerClient producerClient) {
        this.producerClient = producerClient;
    }

    @Override
    public CompletableFuture<Void> sendAsync(Optional<String> key, byte[] payload) {
        EventData event = new EventData(payload);
        event.getProperties().putIfAbsent("producer_timestamp", System.currentTimeMillis());
        return CompletableFuture.runAsync( ()-> producerClient.send(Collections.singleton(event)), executorService);
    }

    @Override
    public void close() throws Exception {
        log.warn("Got command to close EventHubProducerClient");
        executorService.shutdownNow();
        if(executorService.isTerminated()){
            log.info("Executor is in terminated state. Closing Producer");
            producerClient.close();
        }
    }
}
