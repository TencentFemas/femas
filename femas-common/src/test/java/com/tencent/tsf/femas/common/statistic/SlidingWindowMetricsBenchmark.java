package com.tencent.tsf.femas.common.statistic;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
public class SlidingWindowMetricsBenchmark {

    private static final int ITERATION_COUNT = 10;
    private static final int WARMUP_COUNT = 5;
    private static final int THREAD_COUNT = 10;
    private static final int FORK_COUNT = 1;

    SecondsSlidingTimeWindowMetrics metrics = new SecondsSlidingTimeWindowMetrics(5);

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder().build();
        new Runner(options).run();
    }

    @Setup
    public void setUp() {
    }

    @Benchmark
    @Fork(value = FORK_COUNT)
    @Threads(value = THREAD_COUNT)
    @Warmup(iterations = WARMUP_COUNT)
    @Measurement(iterations = ITERATION_COUNT)
    public void directSupplier() {
        metrics.record(100, TimeUnit.MILLISECONDS, Metrics.Outcome.SUCCESS);
    }
}
