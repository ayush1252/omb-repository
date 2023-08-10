package io.openmessaging.benchmark.pojo.output;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class SnapshotResult extends LatencyResult {

  public long timeSinceTestStartInSeconds;

  public Double publishRate;
  public Double consumeRate;
  public Double publishErrorRate;
  public long backlog;
}
