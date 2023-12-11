package io.openmessaging.benchmark.pojo.inputs;

import com.google.common.base.Preconditions;
import io.openmessaging.benchmark.driver.DriverConfiguration;
import io.openmessaging.benchmark.driver.NamespaceMetadata;
import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
public class BenchmarkingRunArguments {
    //These parameters need to be set at the time of creation of the arguments
    @NonNull String testName;
    String testSuiteName;
    @NonNull DriverConfiguration driver;
    @NonNull Workload workload;
    @NonNull Payload messagePayload;

    //These can be set dynamically afterwards or by the benchmark framework.
    @Setter
    NamespaceMetadata namespaceMetadata;
    @Setter
    String runID;

    WorkerAllocations workerAllocation;

    List<String> tags;
    String runUserId;

    public void validate() {
        //Other necessary parameters are already mentioned as non-null
        workerAllocation.validateAndSetDefaults();
        Preconditions.checkNotNull(namespaceMetadata);
        Preconditions.checkNotNull(runID);
        workload.validate();
        messagePayload.validate();
    }
}
