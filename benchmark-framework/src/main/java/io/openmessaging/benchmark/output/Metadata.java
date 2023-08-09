package io.openmessaging.benchmark.output;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Metadata {
    public String workload;

    public int topics;

    //These details are at a topic level.
    public int partitions;
    public int producerCount;
    public int consumerCount;
    public int consumerGroups;

    public int batchCount;
    public String batchSize;

    public String payload;
    public String namespaceName;

    public List<String> tags;

}
