package io.openmessaging.benchmark.perftestsuite;

import io.openmessaging.benchmark.Arguments;

import java.util.Arrays;
import java.util.Collections;

//Each Test except high throughput test is trying to reach 10MB/s throughput
public class AMQPRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {

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
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-1MBMessage.yaml");
                arguments.output = "AMQPDedicated-XLPayload";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
                arguments.visualizeUsingKusto = false;
            }

            @Override
            public String toString() {
                return "XLPayloadTest";
            }
        };
    }

    public static Runnable SmallPayloadTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-4KB.yaml");
                arguments.output = "AMQPDedicated-SmallPayload";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
                arguments.visualizeUsingKusto = false;
            }

            @Override
            public String toString() {
                return "SmallPayloadTest";
            }
        };
    }

    public static Runnable HighThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2-asyncproducer.yaml");
                arguments.workloads = Collections.singletonList("workloads/40producer-40consumer-1MBMessage-HighThroughput.yaml");
                arguments.output = "AMQPDedicated-HighThroughput";
                arguments.tags = Arrays.asList("Benchmarking", "Regression", "Latency");
                arguments.visualizeUsingKusto = false;
            }

            @Override
            public String toString() {
                return "HighThroughputTest";
            }
        };
    }
}
