package io.openmessaging.benchmark.output;

public class SnapshotResult extends LatencyResult{

    public long timeSinceTestStartInSeconds;

    public Double publishRate;
    public Double consumeRate;
    public long backlog;
}
