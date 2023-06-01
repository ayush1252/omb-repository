package io.openmessaging.benchmark.perftestsuite.dedicatedV2;

import io.openmessaging.benchmark.Arguments;
import io.openmessaging.benchmark.perftestsuite.EventHubTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

public class MultiTopicRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "MultiTopicRegressionTest";

        //Add a list of tests here
        configuredTestList.add(MultiTopicMediumThroughputAMQP());
        configuredTestList.add(MultiTopicMediumThroughputKafka());
        //This will run each test 1 by 1
        runPerformanceTests();
    }

    public static Runnable MultiTopicMediumThroughputKafka() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-5tp-50Kb-100Mbps.yaml");;
                arguments.output = "MultiTopicMediumThroughput-Kafka";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), MultiTopic.toString());
            }

            @Override
            public String toString() {
                return "MultiTopicMediumThroughputKafka";
            }
        };
    }

    public static Runnable MultiTopicMediumThroughputAMQP() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-5tp-50Kb-100Mbps.yaml");
                arguments.output = "MultiTopicMediumThroughput-AMQP";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), MultiTopic.toString());
            }

            @Override
            public String toString() {
                return "MultiTopicMediumThroughputAMQP";
            }
        };
    }
}
