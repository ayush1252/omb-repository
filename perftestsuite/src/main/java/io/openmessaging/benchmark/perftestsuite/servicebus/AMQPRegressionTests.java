package io.openmessaging.benchmark.perftestsuite.servicebus;

import io.openmessaging.benchmark.perftestsuite.EventHubTestBase;
import io.openmessaging.benchmark.pojo.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static io.openmessaging.benchmark.perftestsuite.MetadataTags.*;

public class AMQPRegressionTests extends EventHubTestBase {

    public static void main(String[] args) {
        configuredTestList = new ArrayList<>();
        testSuiteName = "AMQPRegressionTests";

        configuredTestList.add(SmallPayloadTest());

        // This will run each test 1 by 1
        runPerformanceTests();
    }

    public static Runnable SmallPayloadTest() {
        return new Runnable() {
            @Override
            public void run() {
                arguments = new Arguments();
                arguments.drivers =
                        Collections.singletonList("driver-azure-servicebus/amqp-batch-standard.yaml");
                arguments.workloads =
                        Collections.singletonList("workloads/1producer-1consumer-1MBMessage-sb.yaml");
                arguments.output = "SmallPayload-AMQPDedicated";
                arguments.tags = Arrays.asList(Regression.toString(), Latency.toString(), Batch.toString());
            }

            @Override
            public String toString() {
                return "SmallPayload-AMQPDedicated";
            }
        };
    }
}
