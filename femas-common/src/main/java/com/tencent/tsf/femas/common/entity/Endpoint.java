package com.tencent.tsf.femas.common.entity;

import java.util.Map;

/**
 * @author zhixinzxliu
 */
public class Endpoint {

    String id;
    String host;
    Integer port;
    Byte status;

    /**
     * 由实例所在应用注册时将SYS_TAG塞入
     */
    Map<String, String> metadata;

    /**
     * 有些注册中心支持基于tags的selector
     * 存放业务自己的tag
     */
    Map<String, String> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", metadata=" + metadata +
                ", tags=" + tags +
                '}';
    }
}
