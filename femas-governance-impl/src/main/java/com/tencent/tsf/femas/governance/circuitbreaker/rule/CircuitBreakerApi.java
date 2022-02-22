package com.tencent.tsf.femas.governance.circuitbreaker.rule;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhixinzxliu
 */
public class CircuitBreakerApi implements Serializable {

    /**
     * 这里如果是Http的话，需要自行将method和path组合起来
     */
    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public boolean validate() {
        if (StringUtils.isBlank(method)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CircuitBreakerApi{" +
                "method='" + method + '\'' +
                '}';
    }
}
