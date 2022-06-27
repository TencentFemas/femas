package com.tencent.tsf.femas.common.statistic;


import com.tencent.tsf.femas.common.util.TimeUtil;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link Metrics} implementation is backed by a sliding time window that aggregates only the
 * calls made in the last {@code N} seconds.
 * <p>
 * The sliding time window is implemented with a circular array of {@code N} partial aggregations
 * (buckets). If the time window size is 10 seconds, the circular array has always 10 partial
 * aggregations (buckets). Every bucket aggregates the outcome of all calls which happen in a
 * certain epoch second. (Partial aggregation) The head bucket of the circular array stores the call
 * outcomes of the current epoch second. The other partial aggregations store the call outcomes of
 * the previous {@code N-1} epoch seconds.
 * <p>
 * The sliding window does not store call outcomes (tuples) individually, but incrementally updates
 * partial aggregations (bucket) and a total aggregation. The total total aggregation is updated
 * incrementally when a new call outcome is recorded. When the oldest bucket is evicted, the partial
 * total aggregation of that bucket is subtracted from the total aggregation. (Subtract-on-Evict)
 * <p>
 * The time to retrieve a Snapshot is constant 0(1), since the Snapshot is pre-aggregated and is
 * independent of the time window size. The space requirement (memory consumption) of this
 * implementation should be nearly constant O(n), since the call outcomes (tuples) are not stored
 * individually. Only {@code N} partial aggregations and 1 total total aggregation are created.
 */
public class SlidingTimeWindowMetrics implements Metrics {

    final PartialAggregation[] partialAggregations;
    private final TotalAggregation totalAggregation;
    private final ReentrantLock updateLock = new ReentrantLock();
    protected int windowInterval;
    protected int windowCount;
    protected int slidingWindowLength;
    volatile int headIndex;

    /**
     * 相同滑动窗口时间，window个数越多，精度越高，但是开销越大
     *
     * @param windowCount window个数
     * @param slidingWindowLength 滑动窗口时间
     */
    public SlidingTimeWindowMetrics(int windowCount, int slidingWindowLength) {
        this.windowInterval = slidingWindowLength / windowCount;
        this.slidingWindowLength = slidingWindowLength;
        this.windowCount = windowCount;
        this.partialAggregations = new PartialAggregation[windowCount];

        long epochMillis = TimeUtil.currentTimeMillis();
        for (int i = 0; i < windowCount; i++) {
            this.partialAggregations[i] = new PartialAggregation(epochMillis);
            epochMillis += windowInterval;
        }

        this.totalAggregation = new TotalAggregation();
    }

    public void record(Outcome outcome) {
        totalAggregation.record(outcome);
        moveWindowToCurrentEpoch(getLatestPartialAggregation()).record(outcome);
    }

    @Override
    public Snapshot record(long duration, TimeUnit durationUnit, Outcome outcome) {
        totalAggregation.record(duration, durationUnit, outcome);
        moveWindowToCurrentEpoch(getLatestPartialAggregation()).record(duration, durationUnit, outcome);
        return new SnapshotImpl(totalAggregation);
    }

    @Override
    public Snapshot getSnapshot() {
        moveWindowToCurrentEpoch(getLatestPartialAggregation());
        return new SnapshotImpl(totalAggregation);
    }

    /**
     * Moves the end of the time window to the current epoch second. The latest bucket of the
     * circular array is used to calculate how many seconds the window must be moved. The difference
     * is calculated by subtracting the epoch second from the latest bucket from the current epoch
     * second. If the difference is greater than the time window size, the time window size is
     * used.
     *
     * @param latestPartialAggregation the latest partial aggregation of the circular array
     */
    private PartialAggregation moveWindowToCurrentEpoch(PartialAggregation latestPartialAggregation) {
        if (!needMoveWindow(latestPartialAggregation)) {
            return latestPartialAggregation;
        }

        updateLock.lock();
        try {
            // double check
            if (!needMoveWindow(getLatestPartialAggregation())) {
                return getLatestPartialAggregation();
            }

            // refresh differenceInMillis
            long currentEpochMillis = TimeUtil.currentTimeMillis();
            long differenceInMillis = currentEpochMillis - latestPartialAggregation.getEpochMillis();
            long nextWindowEpochMillis = getNextWindowEpochMillis();
            long millisToMoveTheWindow = Math.min(differenceInMillis, slidingWindowLength);
            int moveWindowCount = (int) millisToMoveTheWindow / windowInterval;
            PartialAggregation currentPartialAggregation;
            do {
                moveHeadIndexByOne();
                currentPartialAggregation = getLatestPartialAggregation();
                totalAggregation.removeBucket(currentPartialAggregation);
                currentPartialAggregation.reset(nextWindowEpochMillis - (moveWindowCount-- * windowInterval));
            } while (moveWindowCount > 0);

            return currentPartialAggregation;
        } finally {
            updateLock.unlock();
        }
    }

    private boolean needMoveWindow(PartialAggregation latestPartialAggregation) {
        long currentEpochMillis = TimeUtil.currentTimeMillis();

        long differenceInMillis = currentEpochMillis - latestPartialAggregation.getEpochMillis();
        if (differenceInMillis < windowInterval) {
            return false;
        }

        return true;
    }

    private long getNextWindowEpochMillis() {
        long currentEpochMillis = TimeUtil.currentTimeMillis();
        long latestEpochMillis = getLatestPartialAggregation().getEpochMillis();

        while (latestEpochMillis < currentEpochMillis) {
            latestEpochMillis += windowInterval;
        }

        return latestEpochMillis;
    }

    /**
     * Returns the head partial aggregation of the circular array.
     *
     * @return the head partial aggregation of the circular array
     */
    private PartialAggregation getLatestPartialAggregation() {
        return partialAggregations[headIndex];
    }

    /**
     * Moves the headIndex to the next bucket.
     */
    void moveHeadIndexByOne() {
        this.headIndex = (headIndex + 1) % windowCount;
    }
}