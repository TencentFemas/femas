package com.tencent.tsf.femas.governance.ratelimit;


import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.plugin.Plugin;

public interface RateLimiter<T> extends Plugin<T> {

    public boolean acquire();

    public void buildCollector(Service service);

    public boolean acquire(int permits);

    public void update(T rule);

    public void setInsId(String insId);

}
