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

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import io.openmessaging.benchmark.driver.BenchmarkConsumer;
import io.openmessaging.benchmark.driver.ConsumerCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBusBenchmarkConsumer implements BenchmarkConsumer {
  private static final Logger log = LoggerFactory.getLogger(ServiceBusBenchmarkConsumer.class);

  private final ServiceBusProcessorClient eventProcessorClient;
  private final ExecutorService executor;
  private final Future<?> consumerTask;

  public ServiceBusBenchmarkConsumer(ServiceBusProcessorClient eventProcessorClient) {
    this.eventProcessorClient = eventProcessorClient;
    this.executor = Executors.newSingleThreadExecutor();
    this.consumerTask = this.executor.submit(eventProcessorClient::start);
  }

  public static void processEvent(
      ServiceBusReceivedMessageContext eventContext, ConsumerCallback consumerCallback) {
    consumerCallback.messageReceived(
        eventContext.getMessage().getBody().toBytes(),
        TimeUnit.MILLISECONDS.toNanos(
            Long.parseLong(eventContext.getMessage().getEnqueuedTime().toString())));
    eventContext.complete();
  }

  @Override
  public void close() throws Exception {
    log.warn("Shutting down EventHubConsumer gracefully");
    executor.shutdown();
    consumerTask.get();
    eventProcessorClient.stop();
  }
}
