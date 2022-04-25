package com.tencent.tsf.femas.endpoint.adaptor;


import com.tencent.tsf.femas.context.ApplicationContextHelper;

/**
 * @author leo
 */
public abstract class AbstractBaseEndpoint {

    protected final ControllerExecutorTemplate executor;

    public AbstractBaseEndpoint() {
        executor = ApplicationContextHelper.getBean(ControllerExecutorTemplate.class);
    }

}