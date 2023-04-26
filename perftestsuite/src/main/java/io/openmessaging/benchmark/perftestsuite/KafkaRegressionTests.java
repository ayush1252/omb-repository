package io.openmessaging.benchmark.perftestsuite;

import io.openmessaging.benchmark.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class KafkaRegressionTests  extends EventHubTestBase{

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "KafkaRegressionTest";

        //Add a list of tests here
        configuredTestList.add(XLPayloadTest());
        configuredTestList.add(SmallPayloadTest());
        configuredTestList.add(HighThroughputTest());

        //This will run each test 1 by 1
        runPerformanceTests();
    }

    public static Runnable XLPayloadTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-1MBMessage.yaml");
                arguments.output = "KafkaDedicated-XLPayload";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
            }

            @Override
            public String toString() {
                return "Kafka-XLPayloadTest";
            }
        };
    }

    public static Runnable SmallPayloadTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-4KB.yaml");
                arguments.output = "KafkaDedicated-SmallPayload";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
            }

            @Override
            public String toString() {
                return "Kafka-SmallPayloadTest";
            }
        };
    }

    public static Runnable HighThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/40producer-40consumer-1MBMessage-HighThroughput.yaml");
                arguments.output = "KafkaDedicated-HighThroughput";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
            }

            @Override
            public String toString() {
                return "Kafka-HighThroughputTest";
            }
        };
    }
}
