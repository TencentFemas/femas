package com.tencent.tsf.femas.entity.pass;

import com.google.gson.annotations.SerializedName;
import java.nio.charset.Charset;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/1 16:30
 * @Version 1.0
 */
public class GetValue {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @SerializedName("Key")
    private String key;
    @SerializedName("Value")
    private String value;

    public GetValue() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

