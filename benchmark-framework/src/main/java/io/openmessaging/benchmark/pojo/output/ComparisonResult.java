package io.openmessaging.benchmark.pojo.output;

import lombok.Builder;

@Builder
public class ComparisonResult {
    String metricName;
    double currentValue;
    double expectedValue;
}
