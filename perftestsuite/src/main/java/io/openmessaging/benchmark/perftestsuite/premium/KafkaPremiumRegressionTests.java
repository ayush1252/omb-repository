package io.openmessaging.benchmark.perftestsuite.premium;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

import io.openmessaging.benchmark.pojo.Arguments;
import io.openmessaging.benchmark.perftestsuite.EventHubTestBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class KafkaPremiumRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "KafkaPremiumRegressionTest";

        //Add a list of tests here
        configuredTestList.add(XLPayloadTest());
        configuredTestList.add(SmallPayloadTest());

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
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-1MBMessage.yaml");
                arguments.output = "XLPayload-KafkaPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "XLPayload-KafkaPremium";
            }
        };
    }

    public static Runnable SmallPayloadTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/10producer-10consumer-4KB.yaml");
                arguments.output = "SmallPayload-KafkaPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "SmallPayload-KafkaPremium";
            }
        };
    }

    public static Runnable HighThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/40producer-40consumer-100Partitions-1MBMessage-HighThroughput.yaml");
                arguments.output = "HighThroughput-KafkaPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString());
            }

            @Override
            public String toString() {
                return "HighThroughput-KafkaPremium";
            }
        };
    }

    public static Runnable LowThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/1producer-1consumer-50Kb-1Mbps.yaml");
                arguments.output = "LowThroughput-KafkaPremium";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "LowThroughput-KafkaPremium";
            }
        };
    }

    public static Runnable MediumThroughputTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-kafka/kafka-premium.yaml");
                arguments.workloads = Collections.singletonList("workloads/20producer-20consumer-50Kb-50Mbps.yaml");
                arguments.output = "MediumThroughputTest-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Throughput.toString(), Latency.toString());
            }

            @Override
            public String toString() {
                return "MediumThroughputTest-KafkaPremium";
            }
        };
    }
}
