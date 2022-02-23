package com.tencent.tsf.femas.common.serialize;

/**
 * leoziltong
 * 序列化接口类
 */
public interface Serializer {


    byte[] serialize(Object obj);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    String serializeStr(Object obj);

    <T> T deserializeStr(Class<T> clazz, String bytes);

}
