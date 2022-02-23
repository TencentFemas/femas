package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Ticker;
import com.tencent.tsf.femas.common.util.TimeUtil;
import java.util.concurrent.TimeUnit;

public class FemasTokenBucket {

    private volatile long durationInNanos; // 周期长度，单位是秒

    // 当前周期的开始时间（Nanosecond)，与 UNIX timestamp 无关
    private long currentPeriodStartAt;

    private volatile long capacity; // 令牌桶的大小
    private volatile long size; // 令牌桶的令牌存量
    private long capacityDebuffForTokenRefill; // 用来影响令牌发送速度，并不影响令牌桶大小

    private Ticker ticker = Ticker.systemTicker();
    private long lastRefillTime; // 上一次充令牌的时间

    // [上报用] 上报所用的标识
    private String reportId;

    /**
     * 初始化一个令牌桶。
     *
     * @param capacity 容量
     * @param duration 周期长度
     * @param durationUnit 周期单位
     */
    public FemasTokenBucket(long capacity, long duration, TimeUnit durationUnit, String reportId) {
        long absoluteNow = TimeUtil.currentTimeMillis();

        long relativeNow = ticker.read();

        long durationInMillis = durationUnit.toMillis(duration);
        long offset = absoluteNow % durationInMillis;

        this.currentPeriodStartAt = relativeNow - TimeUnit.MILLISECONDS.toNanos(offset);
        this.lastRefillTime = relativeNow;

        this.durationInNanos = durationUnit.toNanos(duration);
        this.capacity = capacity;
        this.size = capacity / 2;

        this.capacityDebuffForTokenRefill = 0;
        this.reportId = reportId;
    }

    public FemasTokenBucket() {
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public long getCapacityDebuffForTokenRefill() {
        return capacityDebuffForTokenRefill;
    }

    public void setCapacityDebuffForTokenRefill(long capacityDebuffForTokenRefill) {
        this.capacityDebuffForTokenRefill = capacityDebuffForTokenRefill;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FemasTokenBucket{" + "duration=" + TimeUnit.NANOSECONDS.toSeconds(durationInNanos) + "s" + ", capacity="
                + capacity + ", size=" + size + '}';
    }

    public synchronized boolean consumeToken() {
        return consumeToken(1);
    }

    public synchronized boolean consumeToken(long numTokens) {
        checkArgument(numTokens > 0, "Number of tokens to consume must be positive");
        checkArgument(numTokens <= capacity,
                "Number of tokens to consume must be less than the capacity of the bucket.");

        refillAndSyncPeriod();

        if (numTokens <= size) {
            size -= numTokens;
            return true;
        }
        return false;
    }

    // 如果处理一次请求时，需要依次 consume 两个 bucket（比如先消费服务相关的 bucket，再消费全局的，
    // 这个时候可能后消费的 bucket 失败了，导致请求被拦住，这时候你需要把先消费的 bucket 的 token 还回去。
    public synchronized void returnToken() {
        returnToken(1);
    }

    public synchronized void returnToken(long numTokens) {
        checkArgument(numTokens > 0, "Number of tokens to return must be positive");

        refillAndSyncPeriod();
        size = Math.min(capacity, size + numTokens);
    }

    public synchronized void refillAndSyncPeriod() {
        // 补充 token，以及维护周期变化时的逻辑
        //
        // 这次场景下应该先 refill，以保证上一次 refill 到这次，不会有升降额带来的速率变化：
        // - 升降额前
        // - 消费 token 时
        long now = ticker.read();

        // 因为降额操作只对本周期实施，需要区分本周期的时间流逝和下周期开始的时间流逝
        long timeElapsedInCurrentPeriodAfterLastRefill, timeElapsedInNextPeriods;

        if (now > currentPeriodStartAt + durationInNanos) {
            // 跨周期了
            // [1]: timeElapsedInCurrentPeriodAfterLastRefill
            // [2]: timeElapsedInNextPeriods
            //
            // [2]
            // |<-- [1] -->|<-->|
            // |-----------------|-----------------|
            // ^ ^
            // | now
            // lastRefillTime
            timeElapsedInCurrentPeriodAfterLastRefill = currentPeriodStartAt + durationInNanos - lastRefillTime;
            timeElapsedInNextPeriods = now - (currentPeriodStartAt + durationInNanos);
        } else {
            // 没有跨周期
            // [1]: timeElapsedInCurrentPeriodAfterLastRefill
            //
            // |<- [1] ->|
            // |-----------------|-----------------|
            // ^ ^
            // | now
            // lastRefillTime
            timeElapsedInCurrentPeriodAfterLastRefill = now - lastRefillTime;
            timeElapsedInNextPeriods = 0;
        }

        double ratioTimeElapsedInCurrentPeriodAfterLastRefill = (double) timeElapsedInCurrentPeriodAfterLastRefill
                / durationInNanos;
        double ratioTimeElapsedInNextPeriods = (double) timeElapsedInNextPeriods / durationInNanos;

        long virtualCapacity = capacity - capacityDebuffForTokenRefill;
        long numTokensToFillInCurrentPeriod = (long) (ratioTimeElapsedInCurrentPeriodAfterLastRefill * virtualCapacity);
        long numTokensToFillInNextPeriods = (long) ratioTimeElapsedInNextPeriods * capacity;
        long numTokensToFill = numTokensToFillInCurrentPeriod + numTokensToFillInNextPeriods;

        size = Math.min(capacity, size + numTokensToFill);
        if (virtualCapacity != 0) {
            // [带 debuff 的充值过程，耗费的时间] = token 折算成时间比例[1] x 整段时间 durationInNanos
            // [不带 debuff 的充值过程，耗费的时间] = token 折算成时间比例[2] x 整段时间 durationInNanos
            // [1]: numTokensToFillInCurrentPeriod / virtualCapacity
            // [2]: numTokensToFillInNextPeriods / capacity
            lastRefillTime += numTokensToFillInCurrentPeriod * durationInNanos / virtualCapacity
                    + numTokensToFillInNextPeriods * durationInNanos / capacity;
        } else {
            // debuff 最大时，当前周期都产生不了新 token，于是白白浪费掉
            lastRefillTime += timeElapsedInCurrentPeriodAfterLastRefill
                    + numTokensToFillInNextPeriods * durationInNanos / capacity;
        }

        long numPeriodsElapsed = (now - currentPeriodStartAt) / durationInNanos;
        if (numPeriodsElapsed > 0) {
            // 跨周期了，把升降额因子调整下
            capacityDebuffForTokenRefill = 0;
            currentPeriodStartAt += numPeriodsElapsed * durationInNanos;
        }
    }

    public synchronized void setNewCapacity(long newCapacity) {
        // 周期相同，做升额降额操作。
        // 升降额操作的目标是把多出来的或者减掉的令牌数，合理地分配到 已有的令牌桶 和 待发放的令牌桶中。
        //
        // 升额时：
        // - 可使用 += (周期内已流逝时间 / 周期时间) * 增量
        // - 待发放 += (周期内剩余时间 / 周期时间) * 增量
        // - 发放速率上升到新配额
        //
        // 降额时：
        // - if (可使用+待发放) < 减量，则 可使用 => 0，待发送 => 0，这个周期不再对用户服务
        // - 可使用 -= (可使用 / (可使用+待发放)) * 减量
        // - 发放速度下降到新配额
        refillAndSyncPeriod();

        if (newCapacity == this.capacity) {
            return;
        }

        long now = ticker.read();
        // 因为前面 refillAndSyncPeriod() 了，只有非常少见的情况下会大于 1.0
        double ratioCurrentPeriodPassed = Math.min((double) (now - currentPeriodStartAt) / durationInNanos, 1.0);

        long capacityDelta = newCapacity - this.capacity;
        if (capacityDelta > 0) {
            long tokensFilled = (long) (ratioCurrentPeriodPassed * capacityDelta);
            this.size += tokensFilled;
        } else {
            long numTokenToReturn = -capacityDelta;

            double tokensToFillInCurrentPeriod = (1.0 - ratioCurrentPeriodPassed) * capacity;
            if (this.size + tokensToFillInCurrentPeriod < numTokenToReturn) {
                this.size = 0;
                this.capacityDebuffForTokenRefill = -newCapacity;
            } else {
                // 当前库存应该出的比例
                double ratioBucketOffered = (double) size / (size + tokensToFillInCurrentPeriod);
                long tokensToReturnInBucket = Math.max(
                        // 按比例扣
                        (long) (numTokenToReturn * ratioBucketOffered),
                        // 如果 size 比较满，应该扣 size 多一点，按比例扣之后 size 可能会大于 capacity。此时应该扣 size 多一点
                        numTokenToReturn - (capacity - size));
                long tokensToReturnInTheFuture = numTokenToReturn - tokensToReturnInBucket;

                this.size -= tokensToReturnInBucket;
                this.capacityDebuffForTokenRefill = (long) ((double) tokensToReturnInTheFuture
                        / tokensToFillInCurrentPeriod * capacity);
            }
        }
        this.capacity = newCapacity;
    }

    private long getNearestPeriodStartTime() {
        long now = TimeUtil.currentTimeMillis();
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNanos);
        return now / durationInMillis * durationInMillis;
    }
}
