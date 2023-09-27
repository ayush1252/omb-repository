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
    @NonNull String testSuiteName;
    @NonNull DriverConfiguration driver;
    @NonNull Workload workload;
    @NonNull Payload messagePayload;

    //These can be set dynamically afterwards or by the benchmark framework.
    @Setter NamespaceMetadata namespaceMetadata;
    @Setter String runID;

    int producerWorkers;
    List<String> workers;

    List<String> tags;
    String runUserId;

    public void validate() {
        //Other necessary parameters are already mentioned as non-null
        Preconditions.checkNotNull(namespaceMetadata);
        Preconditions.checkNotNull(runID);
        workload.validate();
        messagePayload.validate();

        if (workers == null || workers.isEmpty()) {
            Preconditions.checkArgument(producerWorkers == 0);
        }
    }
}
