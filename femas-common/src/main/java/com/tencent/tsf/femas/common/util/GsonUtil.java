package com.tencent.tsf.femas.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.tsf.femas.common.codec.EscapeNonAsciiWriter;
import java.io.StringWriter;
import org.apache.commons.lang3.StringUtils;

public class GsonUtil {

    private static Gson DESERIALIZE_GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static Gson SERIALIZE_GSON = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation()
            .create();


    public static <T> T deserialize(String content, Class<T> clazz) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        return DESERIALIZE_GSON.fromJson(content, clazz);
    }

    // TODO 编写一个可复用的Writer(reset byte数组)
    public static <T> String serializeToJson(T object) {
        StringWriter writer = new StringWriter();
        EscapeNonAsciiWriter escapeWriter = new EscapeNonAsciiWriter(writer);

        SERIALIZE_GSON.toJson(object, escapeWriter);
        return writer.toString();
    }

    public static <T> String toJson(T object) {
        return SERIALIZE_GSON.toJson(object);
    }
}
