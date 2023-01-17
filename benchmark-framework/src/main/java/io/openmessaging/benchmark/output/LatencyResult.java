package io.openmessaging.benchmark.output;

import org.HdrHistogram.Histogram;
import org.apache.commons.math3.util.Precision;

public class LatencyResult {

    public String uuid;
    public String timestamp;

    public Double publishLatencyAvg;
    public Double publishLatency95pct;
    public Double publishLatency99pct;
    public Double publishLatency999pct;
    public Double publishLatency9999pct;
    public Double publishLatencyMax;

    public Double endToEndLatencyAvg;
    public Double endToEndLatency95pct;
    public Double endToEndLatency99pct;
    public Double endToEndLatency999pct;
    public Double endToEndLatency9999pct;
    public Double endToEndLatencyMax;

    public void populatePublishLatency(Histogram publishLatency) {
        this.publishLatencyAvg = microsToMillis(publishLatency.getMean());
        this.publishLatencyAvg = microsToMillis(publishLatency.getMean());
        this.publishLatency95pct = microsToMillis(publishLatency.getValueAtPercentile(95));
        this.publishLatency99pct = microsToMillis(publishLatency.getValueAtPercentile(99));
        this.publishLatency999pct = microsToMillis(publishLatency.getValueAtPercentile(99.9));
        this.publishLatency9999pct = microsToMillis(publishLatency.getValueAtPercentile(99.99));
        this.publishLatencyMax = microsToMillis(publishLatency.getMaxValue());
    }

    public void populateE2ELatency(Histogram endToEndLatency) {
        this.endToEndLatencyAvg = microsToMillis(endToEndLatency.getMean());
        this.endToEndLatency95pct = microsToMillis(endToEndLatency.getValueAtPercentile(95));
        this.endToEndLatency99pct = microsToMillis(endToEndLatency.getValueAtPercentile(99));
        this.endToEndLatency999pct = microsToMillis(endToEndLatency.getValueAtPercentile(99.9));
        this.endToEndLatency9999pct = microsToMillis(endToEndLatency.getValueAtPercentile(99.99));
        this.endToEndLatencyMax = microsToMillis(endToEndLatency.getMaxValue());
    }

    public static double microsToMillis(double microTime) {
        return Precision.round(microTime / (1000),2);
    }
}
