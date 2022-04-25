package com.tencent.tsf.femas.endpoint.adaptor;


/**
 * @author leo
 */
public interface Processor<T> {

    T execute();

    //    boolean checkParams(Object var);

//    void afterProcess(Object var);

}
