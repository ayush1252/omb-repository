package io.openmessaging.benchmark.perftestsuite.dedicatedV2;

import io.openmessaging.benchmark.perftestsuite.EventHubTestBase;
import io.openmessaging.benchmark.pojo.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

//Each Test except high throughput test is trying to reach 10MB/s throughput
public class GeoDRBenchmarkingTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "GeoDRBenchmarkingTests";

        configuredTestList.add(LowThroughputGeoDRSyncTestNonBatch());
        configuredTestList.add(LowThroughputGeoDRAsyncTestNonBatch());
        //This will run each test 1 by 1
        runPerformanceTests();
    }

    public static Runnable LowThroughputGeoDRSyncTestNonBatch() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-dedicated-v2-geodrsync.yaml");
                arguments.workloads = Collections.singletonList("workloads/10producer-10consumer-4KB-hub11.yaml");
                arguments.output = "LowThroughputGeoDRSyncTestNonBatch-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), GeoDR.toString());
            }

            @Override
            public String toString() {
                return "LowThroughputGeoDRSyncTestNonBatch-AMQPDedicated";
            }
        };
    }

    public static Runnable LowThroughputGeoDRAsyncTestNonBatch() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers = Collections.singletonList("driver-azure-eventhubs/amqp-dedicated-v2-geodrasync.yaml");
                arguments.workloads = Collections.singletonList("workloads/10producer-10consumer-4KB-hub11.yaml");
                arguments.output = "LowThroughputGeoDRAsyncTestNonBatch-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), GeoDR.toString());
            }

            @Override
            public String toString() {
                return "LowThroughputGeoDRAsyncTestNonBatch-AMQPDedicated";
            }
        };
    }




}
