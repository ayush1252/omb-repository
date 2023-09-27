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

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EventHubsBenchmarkSyncProducer implements BenchmarkProducer {
    private static final Logger log = LoggerFactory.getLogger(EventHubsBenchmarkSyncProducer.class);

    private final EventHubProducerClient producerClient;
    private final int batchCount;
    private final int batchSize;
    private EventDataBatch eventDataBatch;
    private boolean isProducerClosed = false;

    public EventHubsBenchmarkSyncProducer(EventHubProducerClient producerClient, Properties producerProperties) {
        this.producerClient = producerClient;
        this.batchCount = Integer.parseInt(producerProperties.getProperty("batch.count"));
        this.batchSize = Integer.parseInt(producerProperties.getProperty("batch.size"));
        eventDataBatch = producerClient.createBatch(new CreateBatchOptions().setMaximumSizeInBytes(batchSize));
    }

    @Override
    public CompletableFuture<Integer> sendAsync(Optional<String> key, byte[] payload) {
        EventData event = new EventData(payload);
        event.getProperties().putIfAbsent("producer_timestamp", System.currentTimeMillis());
        boolean addSuccessful = eventDataBatch.tryAdd(event);
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isProducerClosed) {
            future.completeExceptionally(new RuntimeException("Producer Client is closed. Failing the send call"));
            return future;
        }
        if (!addSuccessful) {
            final int messagesToBeSent = eventDataBatch.getCount();
            //EventDataBatch is full. Send the existing batch and then add the current data.
            // This will block the producer thread instead of sending it asynchronously like the non batched approach.
            producerClient.send(eventDataBatch);
            eventDataBatch = producerClient.createBatch(new CreateBatchOptions().setMaximumSizeInBytes(batchSize));
            eventDataBatch.tryAdd(event);
            future.complete(messagesToBeSent);
        } else {
            if (eventDataBatch.getCount() >= batchCount) {
                producerClient.send(eventDataBatch);
                eventDataBatch = producerClient.createBatch(new CreateBatchOptions().setMaximumSizeInBytes(batchSize));
                future.complete(batchCount);
            } else {
                future.complete(0);
            }
        }
        return future;
    }

    @Override
    public void close() throws Exception {
        log.warn("Got command to close EventHubProducerClient");
        if (!isProducerClosed) {
            if (eventDataBatch.getCount() > 0) {
                producerClient.send(eventDataBatch);
                eventDataBatch = producerClient.createBatch();
            }
            producerClient.close();
            isProducerClosed = true;
            log.info("Successfully closed EH Producer");
        }
    }
}
