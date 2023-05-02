package io.openmessaging.benchmark.perftestsuite;

import io.openmessaging.benchmark.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

public class KafkaRegressionTests  extends EventHubTestBase{

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "KafkaRegressionTest";

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
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-1MBMessage.yaml");
                arguments.output = "XLPayload-KafkaDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "XLPayload-KafkaDedicated";
            }
        };
    }

    public static Runnable XLPayloadTestNonBatched() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-1MBMessage.yaml");
                arguments.output = "XLPayloadNonBatch-KafkaDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "XLPayloadNonBatch-KafkaDedicated";
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
                arguments.output = "SmallPayload-KafkaDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "SmallPayload-KafkaDedicated";
            }
        };
    }

    public static Runnable SmallPayloadTestNonBatched() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-4KB.yaml");
                arguments.output = "SmallPayloadNonBatch-KafkaDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "SmallPayloadNonBatch-KafkaDedicated";
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
                arguments.output = "HighThroughput-KafkaDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString());
            }

            @Override
            public String toString() {
                return "HighThroughput-KafkaDedicated";
            }
        };
    }

    public static Runnable LowThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-50Kb-1Mbps.yaml");
                arguments.output = "LowThroughput-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "LowThroughput-AMQPDedicated";
            }
        };
    }

    public static Runnable MediumThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-batch-dedicated-v2.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-50Kb-100Mbps.yaml");
                arguments.output = "MediumThroughputTest-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "MediumThroughputTest-AMQPDedicated";
            }
        };
    }
}
