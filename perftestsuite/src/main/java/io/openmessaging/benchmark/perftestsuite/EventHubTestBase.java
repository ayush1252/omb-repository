package io.openmessaging.benchmark.perftestsuite;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import io.openmessaging.benchmark.Benchmark;
import io.openmessaging.benchmark.pojo.Arguments;
import io.openmessaging.benchmark.appconfig.adapter.ConfigProvider;
import io.openmessaging.benchmark.appconfig.adapter.ConfigurationKey;
import io.openmessaging.benchmark.pojo.output.TestResult;
import io.openmessaging.benchmark.storage.adapter.StorageAdapter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public abstract class EventHubTestBase {
    private static final Logger log = LoggerFactory.getLogger(EventHubTestBase.class);

    protected static String testSuiteName = "EventHubTestBase";
    protected static Arguments arguments = new Arguments();
    protected static List<Runnable> configuredTestList = new ArrayList<>();

    static ConfigProvider configProvider;
    public static void runPerformanceTests(){
        //ApplicationInsights.attach();
        log.info("Starting Execution of Test Suite: " + testSuiteName);
        configuredTestList.forEach(individualTest -> {
            configProvider = ConfigProvider.getInstance();
            log.info("Running Test: " + individualTest.toString());
            individualTest.run();


            //Specifying worker roles if configured
            //arguments.workers = getWorkersIfConfigured(testSuiteName);
            arguments.producerWorkers = arguments.workers == null ? 0: arguments.workers.size() /2;
            try {
                //No Validation happening since the current validation is happening in Kusto.
                final List<TestResult> testResults = Benchmark.executeBenchmarkingRun(arguments);
                testResults.forEach(individualResult -> Benchmark.persistTestResults(arguments, individualResult));
            } catch (Exception e) {
                log.error("Failed Execution of Test: " + individualTest, e);
            }
        });
        log.info("Completed Execution of Test - " + testSuiteName);
        exit(0);
    }

    static List<String> getWorkersIfConfigured(String testSuiteName){
        return StorageAdapter
                .readBlobFromStorage(configProvider.getConfigurationValue(ConfigurationKey.StorageAccountName),
                        configProvider.getConfigurationValue(ConfigurationKey.WorkersContainerName),
                        StringUtils.toRootLowerCase(testSuiteName) + "-workerfile", ".txt");
    }
}
