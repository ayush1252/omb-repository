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
package io.openmessaging.benchmark.worker;

import static java.util.stream.Collectors.toList;

import com.beust.jcommander.internal.Maps;
import com.google.common.base.Preconditions;
import io.openmessaging.benchmark.utils.ListPartition;
import io.openmessaging.benchmark.worker.commands.ConsumerAssignment;
import io.openmessaging.benchmark.worker.commands.CountersStats;
import io.openmessaging.benchmark.worker.commands.CumulativeLatencies;
import io.openmessaging.benchmark.worker.commands.PeriodStats;
import io.openmessaging.benchmark.worker.commands.ProducerWorkAssignment;
import io.openmessaging.benchmark.worker.commands.TopicSubscription;
import io.openmessaging.benchmark.worker.commands.TopicsInfo;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistributedWorkersEnsemble implements Worker {

  private static final Logger log = LoggerFactory.getLogger(DistributedWorkersEnsemble.class);
  private final List<Worker> workers;
  private final List<Worker> producerWorkers;
  private final List<Worker> consumerWorkers;
  private final Worker leader;
  private int numberOfUsedProducerWorkers;

  public DistributedWorkersEnsemble(List<Worker> workers, int numberOfUsedProducerWorkers) {
    workers =
        workers.stream()
            .filter(
                p -> {
                  try {
                    p.healthCheck();
                  } catch (Exception e) {
                    log.error(
                        "Found error during health-check of worker role {} - {}",
                        p,
                        e.getMessage());
                    return false;
                  }
                  return true;
                })
            .collect(toList());

    Preconditions.checkArgument(
        workers.size() > 1, "Insufficient count of active workers for the test");

    this.workers = workers;

    leader = this.workers.get(0);
    this.producerWorkers = workers.stream().limit(numberOfUsedProducerWorkers).collect(toList());
    this.consumerWorkers =
        workers.stream().filter(p -> !producerWorkers.contains(p)).collect(toList());

    log.info("Workers list - producers: {}", producerWorkers);
    log.info("Workers list - consumers: {}", consumerWorkers);
  }

  @Override
  public void initializeDriver(File configurationFile) throws IOException {
    workers.parallelStream()
        .forEach(
            w -> {
              try {
                w.initializeDriver(configurationFile);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public List<Topic> createTopics(TopicsInfo topicsInfo) throws IOException {
    return leader.createTopics(topicsInfo);
  }

  @Override
  public void notifyTopicCreation(List<Topic> topics) throws IOException {
    workers.parallelStream()
        .forEach(
            w -> {
              try {
                w.notifyTopicCreation(topics);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void createProducers(List<String> topics) {
    // topics is a normalized list i.e. it accounts for duplicated entries in case
    // of m topics and n producers where m < n. In this case, map the topics as is
    // to honor the number of producers per topic configured for the workload
    List<List<String>> topicsPerProducer;
    if (topics.size() <= producerWorkers.size()) {
      topicsPerProducer = new ArrayList<>();
      for (String topic : topics) {
        List<String> topicList = new ArrayList<>();
        topicList.add(topic);
        topicsPerProducer.add(topicList);
      }
    } else {
      topicsPerProducer = ListPartition.partitionList(topics, producerWorkers.size());
    }

    Map<Worker, List<String>> topicsPerProducerMap = Maps.newHashMap();
    int i = 0;
    for (List<String> assignedTopics : topicsPerProducer) {
      topicsPerProducerMap.put(producerWorkers.get(i++), assignedTopics);
    }

    // Number of actually used workers might be less than available workers
    numberOfUsedProducerWorkers = i;
    log.info("Number of producers configured for the topic: " + numberOfUsedProducerWorkers);
    topicsPerProducerMap.entrySet().parallelStream()
        .forEach(
            e -> {
              try {
                e.getKey().createProducers(e.getValue());
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }

  @Override
  public void startLoad(ProducerWorkAssignment producerWorkAssignment) throws IOException {
    // Reduce the publish rate across all the brokers
    double newRate = producerWorkAssignment.publishRate / numberOfUsedProducerWorkers;
    log.info("Setting worker assigned publish rate to {} msgs/sec", newRate);
    // Reduce the publish rate across all the brokers

    producerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.startLoad(producerWorkAssignment.withPublishRate(newRate));
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void probeProducers() throws IOException {
    producerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.probeProducers();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void adjustPublishRate(double publishRate) throws IOException {
    // Reduce the publish rate across all the brokers
    double newRate = publishRate / numberOfUsedProducerWorkers;
    log.debug("Adjusting producer publish rate to {} msgs/sec", newRate);
    producerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.adjustPublishRate(newRate);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void stopAll() throws IOException {
    workers.parallelStream()
        .forEach(
            worker -> {
              try {
                worker.stopAll();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void pauseConsumers() throws IOException {
    consumerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.pauseConsumers();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void resumeConsumers() throws IOException {
    consumerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.resumeConsumers();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void pauseProducers() throws IOException {
    producerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.pauseProducers();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void resumeProducers() throws IOException {
    producerWorkers.parallelStream()
        .forEach(
            w -> {
              try {
                w.resumeProducers();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void healthCheck() throws IOException {
    workers.parallelStream()
        .forEach(
            w -> {
              try {
                w.healthCheck();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void createConsumers(ConsumerAssignment overallConsumerAssignment) {
    List<List<TopicSubscription>> subscriptionsPerConsumer =
        ListPartition.partitionList(
            overallConsumerAssignment.topicsSubscriptions, consumerWorkers.size());
    Map<Worker, ConsumerAssignment> topicsPerWorkerMap = Maps.newHashMap();
    int i = 0;
    for (List<TopicSubscription> tsl : subscriptionsPerConsumer) {
      ConsumerAssignment individualAssignment = new ConsumerAssignment();
      individualAssignment.topicsSubscriptions = tsl;
      topicsPerWorkerMap.put(consumerWorkers.get(i++), individualAssignment);
    }

    topicsPerWorkerMap.entrySet().parallelStream()
        .forEach(
            e -> {
              try {
                e.getKey().createConsumers(e.getValue());
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }

  @Override
  public PeriodStats getPeriodStats() {
    return workers.parallelStream()
        .map(
            w -> {
              try {
                return w.getPeriodStats();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .reduce(new PeriodStats(), PeriodStats::plus);
  }

  @Override
  public CumulativeLatencies getCumulativeLatencies() {
    return workers.parallelStream()
        .map(
            w -> {
              try {
                return w.getCumulativeLatencies();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .reduce(new CumulativeLatencies(), CumulativeLatencies::plus);
  }

  @Override
  public CountersStats getCountersStats() throws IOException {
    return workers.parallelStream()
        .map(
            w -> {
              try {
                return w.getCountersStats();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .reduce(new CountersStats(), CountersStats::plus);
  }

  @Override
  public void resetStats() throws IOException {
    workers.parallelStream()
        .forEach(
            w -> {
              try {
                w.resetStats();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  public void close() throws Exception {
    for (Worker w : workers) {
      try {
        w.close();
      } catch (Exception ignored) {
        log.trace("Ignored error while closing worker {}", w, ignored);
      }
    }
  }
}
