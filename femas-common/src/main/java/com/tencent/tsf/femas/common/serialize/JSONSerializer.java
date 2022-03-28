package com.tencent.tsf.femas.common.serialize;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * leoziltong
 */
public class JSONSerializer {

    private static final Logger log = LoggerFactory.getLogger(JSONSerializer.class);
    private static volatile ObjectMapper mapper = new ObjectMapper();

    public static byte[] serialize(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("serialize  error ", e);
        }
        return null;
    }


    public static <T> T deserialize(Class<T> clazz, final byte[] bytes) {
        try {
            return mapper.readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("deserialize  error ", e);
        }
        return null;
    }

    public static String serializeStr(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("serializeStr  error ", e);
        }
        return null;
    }

    public static <T> T deserializeStr(Class<T> clazz, final String str) {
        try {
            return mapper.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            log.warn("deserializeStr {} error ", str, e);
        }
        return null;
    }

    public static <T> List<T> deserializeStr2List(Class<T> clazz, final String str) {
        try {
            List<T> list = mapper.readValue(str, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
            return list;
        } catch (JsonProcessingException e) {
            log.error("deserializeStr2List {} error ", str, e);
        }
        return null;
    }

}
