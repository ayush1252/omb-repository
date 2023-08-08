package io.openmessaging.benchmark.pojo.output;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SnapshotResult extends LatencyResult {

  public long timeSinceTestStartInSeconds;

  public Double publishRate;
  public Double consumeRate;
  public Double publishErrorRate;
  public long backlog;
}
