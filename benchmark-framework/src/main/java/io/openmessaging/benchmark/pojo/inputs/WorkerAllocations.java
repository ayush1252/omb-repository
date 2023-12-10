package io.openmessaging.benchmark.pojo.inputs;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class WorkerAllocations {

    List<String> totalWorkerNodes;
    List<String> producerWorkerNodes;
    List<String> consumerWorkerNodes;

    @Builder.Default
    boolean isRemoteWorkerRequired = false;

    @Builder.Default
    int producerWorkerNodeCount = -1;

    public void validateAndSetDefaults() {
        if (isRemoteWorkerRequired) {
            if (CollectionUtils.isEmpty(producerWorkerNodes) && CollectionUtils.isEmpty(consumerWorkerNodes) && CollectionUtils.isEmpty(totalWorkerNodes)) {
                throw new IllegalArgumentException("Worker List options cannot be null in-case remoteWorker execution is required");
            }

            if (CollectionUtils.isNotEmpty(totalWorkerNodes)) {
                if (CollectionUtils.isNotEmpty(producerWorkerNodes) || CollectionUtils.isNotEmpty(consumerWorkerNodes)) {
                    throw new IllegalArgumentException("Conflict found. Either set total worker nodes or set individual list of producer and consumer workers.");
                }
                producerWorkerNodeCount = producerWorkerNodeCount != -1 ? producerWorkerNodeCount : (totalWorkerNodes.size()) / 2;
            }
        }
    }
}
