package io.openmessaging.benchmark.perftestsuite.premium;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

import io.openmessaging.benchmark.Arguments;
import io.openmessaging.benchmark.perftestsuite.EventHubTestBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MultiTopicPremiumRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "MultiTopicPremiumRegressionTest";

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
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-5tp-50Kb-50Mbps.yaml");;
                arguments.output = "MultiTopicMediumThroughput-KafkaPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), MultiTopic.toString());
            }

            @Override
            public String toString() {
                return "MultiTopicMediumThroughputKafkaPremium";
            }
        };
    }

    public static Runnable MultiTopicMediumThroughputAMQP() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-5tp-50Kb-50Mbps.yaml");
                arguments.output = "MultiTopicMediumThroughput-AMQPPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), MultiTopic.toString());
            }

            @Override
            public String toString() {
                return "MultiTopicMediumThroughputAMQPPremium";
            }
        };
    }
}
