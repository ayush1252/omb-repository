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
                arguments.visualizeUsingKusto = false;
            }

            @Override
            public String toString() {
                return "Kafka-XLPayloadTest";
            }
        };
    }
}
