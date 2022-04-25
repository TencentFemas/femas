package com.tencent.tsf.femas.entity.pass.circuitbreaker;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerApi implements Serializable {

    private String apiId;

    private String path;

    private String method;

    private String strategyId;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(final String apiId) {
        this.apiId = apiId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(final String strategyId) {
        this.strategyId = strategyId;
    }

    public boolean validate() {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(method)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CircuitBreakerApi{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
