package com.tencent.tsf.femas.common;

import com.tencent.tsf.femas.common.exception.FemasRegisterDescribeException;

import java.util.function.Supplier;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 11:06
 * @Version 1.0
 */
public interface RegistryBuilder<T> {

    /**
     * 构建client
     *
     * @param serverAddressSupplier 注册地址Supplier
     * @param namespace             命名空间
     * @param certificate           true-调用{@link #certificateClient(Supplier, String, Object...)},false调用{{@link #build(Supplier, String)}}
     * @param args                  参数-调用{@link #certificateClient(Supplier, String, Object...)}的参数
     * @return client
     * @throws FemasRegisterDescribeException
     */
    T describeClient(Supplier<String> serverAddressSupplier, String namespace, boolean certificate, Object... args) throws FemasRegisterDescribeException;

    /**
     * 构建client,与certificateClient的区别是不带参数args
     *
     * @param serverAddressSupplier 注册地址Supplier
     * @param namespace             命名空间
     * @return client
     */
    T build(Supplier<String> serverAddressSupplier, String namespace);

    /**
     * 获得认证的client
     *
     * @param serverAddressSupplier 注册地址Supplier
     * @param namespace             命名空间
     * @param args                  参数
     * @return 认证的Client
     */
    T certificateClient(Supplier<String> serverAddressSupplier, String namespace, Object... args);

    /**
     * 获得注册地址
     *
     * @param serverAddressSupplier 注册地址Supplier
     * @return 注册地址
     */
    default String getKey(Supplier<String> serverAddressSupplier) {
        return serverAddressSupplier.get();
    }

}
