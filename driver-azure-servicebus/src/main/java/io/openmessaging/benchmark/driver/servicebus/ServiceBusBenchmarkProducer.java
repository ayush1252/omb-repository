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

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.servicebus.*;
import io.openmessaging.benchmark.driver.BenchmarkProducer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBusBenchmarkProducer implements BenchmarkProducer {
  private static final Logger log = LoggerFactory.getLogger(ServiceBusBenchmarkProducer.class);

  private final ServiceBusSenderAsyncClient producerClient;
  private final int batchCount;
  private final int batchSize;
  private ArrayList<ServiceBusMessage> eventDataBatch;
  private boolean isProducerClosed = false;
  private final CreateBatchOptions batchOptions;

  public ServiceBusBenchmarkProducer(
      ServiceBusSenderAsyncClient producerClient, Properties producerProperties) {
    this.producerClient = producerClient;
    this.batchCount = Integer.parseInt(producerProperties.getProperty("batch.count"));
    this.batchSize = Integer.parseInt(producerProperties.getProperty("batch.size"));
    batchOptions = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);
    eventDataBatch = new ArrayList<ServiceBusMessage>();
  }

  @Override
  public CompletableFuture<Integer> sendAsync(Optional<String> key, byte[] payload) {

    CompletableFuture<Integer> future = new CompletableFuture<>();
    if (isProducerClosed) {
      future.completeExceptionally(
          new RuntimeException("Producer Client is closed. Failing the send call"));
      return future;
    }

    String strPayload = new String(payload);
    ServiceBusMessage event = new ServiceBusMessage(strPayload);
    eventDataBatch.add(event);
    boolean addSuccessful = eventDataBatch.size() < batchCount;

    if (!addSuccessful) {
      final int messagesToBeSent = eventDataBatch.size();
      // EventDataBatch is full. Send the existing batch and then add the current data.
      producerClient
          .sendMessages(eventDataBatch)
          .subscribe(
              unused -> {}, future::completeExceptionally, () -> future.complete(messagesToBeSent));
      // TODO: Check for mem leak
      eventDataBatch = new ArrayList<ServiceBusMessage>();
    }
    return future;
  }

  @Override
  public void close() throws Exception {
    log.warn("Got command to close EventHubProducerClient");
    if (!isProducerClosed) {
      if (!eventDataBatch.isEmpty()) {
        producerClient.sendMessages(eventDataBatch).block();
        eventDataBatch = new ArrayList<ServiceBusMessage>();
      }
      producerClient.close();
      isProducerClosed = true;
      log.info("Successfully closed EH Producer");
    }
  }
}
