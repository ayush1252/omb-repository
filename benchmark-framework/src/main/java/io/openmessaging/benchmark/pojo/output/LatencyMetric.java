package io.openmessaging.benchmark.pojo.output;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.HdrHistogram.Histogram;
import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Data
public class LatencyMetric {

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

  private static double microsToMillis(double microTime) {
    return Precision.round(microTime / (1000), 2);
  }

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

  /**
   * Comparing two Latency Metric incorporating the acceptability of error denoted by @param errorThreshold
   * The first object should be the one compared to and the other should be the one compared against.
   * @return List which contains details about all the params that failed the comparison.
   */
  public List<ComparisonResult> compareAndEvaluateDiff(LatencyMetric other, double errorThreshold) {
    List<ComparisonResult> result = new ArrayList<>();

    compareAndAppendReason(result, "PublishLatencyPAvg",
            publishLatencyAvg, other.publishLatencyAvg,errorThreshold);
    compareAndAppendReason(result, "PublishLatencyP95",
            publishLatency95pct, other.publishLatency95pct,errorThreshold);
    compareAndAppendReason(result, "PublishLatencyP99",
            publishLatency99pct, other.publishLatency99pct,errorThreshold);
    compareAndAppendReason(result, "PublishLatencyP99.9",
            publishLatency999pct, other.publishLatency999pct,errorThreshold);
    compareAndAppendReason(result, "PublishLatencyP99.99",
            publishLatency9999pct, other.publishLatency9999pct,errorThreshold);
    compareAndAppendReason(result, "PublishLatencyMax",
            publishLatencyMax, other.publishLatencyMax,errorThreshold);

    compareAndAppendReason(result, "E2ELatencyPAvg",
            endToEndLatencyAvg, other.endToEndLatencyAvg,errorThreshold);
    compareAndAppendReason(result, "E2ELatencyP95th",
            endToEndLatency95pct, other.endToEndLatency95pct,errorThreshold);
    compareAndAppendReason(result, "E2ELatencyP99",
            endToEndLatency99pct, other.endToEndLatency99pct,errorThreshold);
    compareAndAppendReason(result, "E2ELatencyP99.9",
            endToEndLatency999pct, other.endToEndLatency999pct,errorThreshold);
    compareAndAppendReason(result, "E2ELatencyP99.99",
            endToEndLatency9999pct, other.endToEndLatency9999pct,errorThreshold);
    compareAndAppendReason(result, "E2ELatencyMax",
            endToEndLatencyMax, other.endToEndLatencyMax,errorThreshold);

    return result;
  }

  private void compareAndAppendReason(List<ComparisonResult> result, String metricName, Double currentValue,
                                      Double expectedValue, Double errorThreshold) {
    if (expectedValue != null && currentValue != null && currentValue > (1 + errorThreshold / 100) * expectedValue) {
      result.add(ComparisonResult.builder()
              .metricName(metricName)
              .currentValue(currentValue)
              .expectedValue(expectedValue)
              .build());
    }
  }
}
