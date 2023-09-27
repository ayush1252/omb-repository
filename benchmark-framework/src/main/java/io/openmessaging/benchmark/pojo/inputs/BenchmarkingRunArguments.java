package io.openmessaging.benchmark.pojo.inputs;

import io.openmessaging.benchmark.driver.DriverConfiguration;
import io.openmessaging.benchmark.driver.NamespaceMetadata;
import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
public class BenchmarkingRunArguments {
    @Setter
    @NonNull String runID;
    @NonNull String testName;
    @NonNull DriverConfiguration driver;
    @NonNull Workload workload;
    @NonNull NamespaceMetadata namespaceMetadata;
    @NonNull Payload messagePayload;

    String testSuiteName;
    int producerWorkers;
    List<String> workers;

    List<String> tags;
    String runUserId;
}
