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

import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class EventHubsBenchmarkProducer implements BenchmarkProducer {

    //    private final EventHubProducerAsyncClient asyncProducer;
    private final EventHubClient eventHubClient;

//    public EventHubsBenchmarkProducer(EventHubProducerAsyncClient asyncProducer) {
//        this.asyncProducer = asyncProducer;
//    }

    public EventHubsBenchmarkProducer(EventHubClient eventHubClient) {
        this.eventHubClient = eventHubClient;
    }

    @Override
    public CompletableFuture<Void> sendAsync(Optional<String> key, byte[] payload) {
//        CompletableFuture<Void> future = new CompletableFuture<>();
//        EventDataBatch eventDataBatch = producer.createBatch();
//
//        if (eventDataBatch.tryAdd(new EventData(payload))) {
//            producer.send(eventDataBatch);
//        } else {
//            Exception e = new IllegalArgumentException("Event is too large for an empty batch. Max size: "
//                    + eventDataBatch.getMaxSizeInBytes());
//            //
//            future.completeExceptionally(e);
//        }
//
//        future.complete(null);
//        return future;


//        CompletableFuture<Void> future = new CompletableFuture<>();
        com.microsoft.azure.eventhubs.EventData event = EventData.create(payload);

        return eventHubClient.send(event).thenApply( unused -> null);


//        CompletableFuture<Void> future = new CompletableFuture<>();
//        Flux<EventData> events = Flux.just(
//                new EventData(payload));
//
//        final CreateBatchOptions options = new CreateBatchOptions();
//        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
//                asyncProducer.createBatch(options).block());
//
//        events.flatMap(event -> {
//            final EventDataBatch batch = currentBatch.get();
//            if (batch.tryAdd(event)) {
//                return Mono.empty();
//            }
//            return Mono.when(
//                    asyncProducer.send(batch),
//                    asyncProducer.createBatch(options).map(newBatch -> {
//                        currentBatch.set(newBatch);
//
//                        // Add that event that we couldn't before.
//                        if (!newBatch.tryAdd(event)) {
//                            throw Exceptions.propagate(new IllegalArgumentException(String.format(
//                                    "Event is too large for an empty batch. Max size: %s. Event: %s",
//                                    newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
//                        }
//
//                        return newBatch;
//                    }));
//        }).then()
//                .doFinally(signal -> {
//                    final EventDataBatch batch = currentBatch.getAndSet(null);
//                    if (batch != null) {
//                        asyncProducer.send(batch).block();
//                    }
//                })
//                .subscribe(unused -> System.out.println("Complete ...................."),
//                        future::completeExceptionally,
//                        () -> future.complete(null));
//
//        return future;
    }

    @Override
    public void close() throws Exception {
        eventHubClient.close();
    }
}
