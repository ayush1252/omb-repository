package io.openmessaging.benchmark.pojo;

import io.openmessaging.benchmark.driver.DriverConfiguration;
import io.openmessaging.benchmark.worker.Worker;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class TestRunInput {
    Arguments inputArguments;
    Worker benchmarkWorker;
    Workload testWorkload;
    DriverConfiguration testDriver;
    UUID testRunID;
}
