package com.tencent.tsf.femas.common.context;

import com.tencent.tsf.femas.common.entity.ErrorStatus;
import com.tencent.tsf.femas.common.statistic.Metrics;

public class RpcContext {

    private ErrorStatus errorStatus;
    private Metrics metrics;
    private TracingContext tracingContext;

    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(ErrorStatus errorStatus) {
        this.errorStatus = errorStatus;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public void setMetrics(Metrics metrics) {
        this.metrics = metrics;
    }

    public TracingContext getTracingContext() {
        return tracingContext;
    }

    public void setTracingContext(TracingContext tracingContext) {
        this.tracingContext = tracingContext;
    }
}
