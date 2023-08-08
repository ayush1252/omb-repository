package io.openmessaging.benchmark.pojo.output;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class LatencyResult {
  public String uuid;
  public String timestamp;
  @Builder.Default public LatencyMetric latencyMetric = new LatencyMetric();
}
