package com.tencent.tsf.femas.storage.rocksdb;

/**
 * @Author leoziltong
 * @Date: 2021/4/19 17:19
 * @Version 1.0
 */
public interface Lifecycle<T> {

    /**
     * 根据配置初始化配置
     *
     * @param conf
     * @throws Exception
     */
    void init(final T conf) throws Exception;

    /**
     * 释放资源
     *
     * @throws Exception
     */
    void close() throws Exception;

}
