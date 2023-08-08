package io.openmessaging.benchmark.pojo.output;

import lombok.Builder;

@Builder
public class LatencyComparisonResult {
    String metricName;
    double currentValue;
    double expectedValue;
}
