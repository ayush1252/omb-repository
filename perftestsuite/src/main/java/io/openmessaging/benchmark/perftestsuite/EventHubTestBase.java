package io.openmessaging.benchmark.perftestsuite;

import io.openmessaging.benchmark.Benchmark;
import io.openmessaging.benchmark.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class EventHubTestBase {
    private static final Logger log = LoggerFactory.getLogger(EventHubTestBase.class);
    static Arguments arguments = new Arguments();
    static List<Runnable> configuredTestList = new ArrayList<>();

    public static void runPerformanceTests(){
        configuredTestList.stream().forEach(individualTest -> {
            log.info("Running Test: " + individualTest.toString());
            individualTest.run();
            try {
                Benchmark.executeBenchmarkingRun(arguments);
            } catch (Exception e) {
                log.error("Failed Execution of Test: " + individualTest.toString(), e);
            }
        });
    }

}
