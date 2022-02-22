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

    T describeClient(Supplier<String> serverAddressSupplier, String namespace, boolean certificate, Object... var) throws FemasRegisterDescribeException;

    T build(Supplier<String> serverAddressSupplier, String namespace);

    T certificateClient(Supplier serverAddressSupplier, String namespace, Object... var);

    default String getKey(Supplier<String> serverAddressSupplier) {
        String key = serverAddressSupplier.get();
        return key;
    }

}
