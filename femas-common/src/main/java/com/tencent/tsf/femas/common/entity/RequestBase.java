package com.tencent.tsf.femas.common.entity;

import java.io.Serializable;

public abstract class RequestBase implements Serializable {

    private static final long serialVersionUID = -7323141575870688636L;

    /**
     * Method name
     */
    private String targetMethod;

    /**
     * Method name signature
     * 用于API 熔断
     */
    private String targetMethodSig;

    /**
     * Argument type strings of method
     */
    private Class[] methodArgSigs;

    /**
     * Argument values of method
     */
    private transient Object[] methodArgs;

    /**
     * Target service unique name, contains interfaceName, uniqueId and etc.
     */
    private Service targetService;

    /**
     * Gets method name.
     *
     * @return the method name
     */
    public String getTargetMethod() {
        return targetMethod;
    }

    public String getTargetMethodSig() {
        return targetMethodSig;
    }

    public void setTargetMethodSig(String targetMethodSig) {
        this.targetMethodSig = targetMethodSig;
    }

    /**
     * Get method args object [ ].
     *
     * @return the object [ ]
     */
    public Object[] getMethodArgs() {
        return methodArgs;
    }

    /**
     * Sets method args.
     *
     * @param methodArgs the method args
     */
    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }

    /**
     * Get method arg sigs string [ ].
     *
     * @return the string [ ]
     */
    public Class[] getMethodArgSigs() {
        return methodArgSigs;
    }

    /**
     * Sets method arg sigs.
     *
     * @param methodArgSigs the method arg sigs
     */
    public void setMethodArgSigs(Class[] methodArgSigs) {
        this.methodArgSigs = methodArgSigs;
    }

    /**
     * Gets target service unique name.
     *
     * @return the target service unique name
     */
    public Service getTargetService() {
        return targetService;
    }

    /**
     * Sets method name.
     *
     * @param methodName the method name
     */
    public void setTargetMethodName(String methodName) {
        this.targetMethod = methodName;
    }

    /**
     * Sets target service unique name.
     *
     * @param targetService the target service unique name
     */
    public void setTargetServiceUniqueName(Service targetService) {
        this.targetService = targetService;
    }

}
