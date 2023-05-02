package io.openmessaging.benchmark.perftestsuite;

import io.openmessaging.benchmark.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

//Each Test except high throughput test is trying to reach 10MB/s throughput
public class AMQPRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "AMQPRegressionTests";

        //Add a list of tests here
        configuredTestList.add(XLPayloadTest());
        configuredTestList.add(XLPayloadTestNonBatched());
        configuredTestList.add(SmallPayloadTest());
        configuredTestList.add(SmallPayloadTestNonBatched());

        //Throughput Tests
        configuredTestList.add(LowThroughputTest());
        configuredTestList.add(MediumThroughputTest());
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
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
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
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "SmallPayloadTest";
            }
        };
    }


    public static Runnable SmallPayloadTestNonBatched() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-4KB.yaml");
                arguments.output = "AMQPDedicated-SmallPayloadNonBatch";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "SmallPayloadTestNonBatch";
            }
        };
    }

    public static Runnable XLPayloadTestNonBatched() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-4KB.yaml");
                arguments.output = "AMQPDedicated-SmallPayloadNonBatch";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "SmallPayloadTestNonBatch";
            }
        };
    }

    public static Runnable HighThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/40producer-40consumer-1MBMessage-HighThroughput.yaml");
                arguments.output = "AMQPDedicated-HighThroughput";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString());
            }

            @Override
            public String toString() {
                return "HighThroughputTest";
            }
        };
    }

    public static Runnable LowThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-50Kb-1Mbps.yaml");
                arguments.output = "AMQPDedicated-LowThroughput";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "LowThroughputTest";
            }
        };
    }

    public static Runnable MediumThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-50Kb-100Mbps.yaml");
                arguments.output = "AMQPDedicated-MediumThroughputTest";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "MediumThroughputTest";
            }
        };
    }
}
