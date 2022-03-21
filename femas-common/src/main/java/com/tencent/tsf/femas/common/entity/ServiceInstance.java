package com.tencent.tsf.femas.common.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServiceInstance implements Serializable {

    Service service;

    String id;
    String host;
    Integer port;
    //注册中心最近更新时间
    Long lastUpdateTime;

    String serviceVersion;

    String clientVersion;

    EndpointStatus status = EndpointStatus.INITIALIZING;
    // 如果是转换过来的，保留原始数据
    Object origin;

    /**
     * 由实例所在应用注册时将SYS_TAG塞入
     * 此处Metadata赋值之后不应当再修改
     */
    Map<String, String> metadata = new HashMap<String, String>();

    /**
     * 有些注册中心支持基于tags的selector
     * 存放业务自己的tag
     */
    Map<String, String> tags = new HashMap<String, String>();

    /**
     * 是否开启心跳上报
     */
    Boolean heartBeat;

    /**
     * 上报时间间隔
     */
    Integer ttl;

    public ServiceInstance() {
    }

    public ServiceInstance(Service service, String id, String host, Integer port, EndpointStatus status, Map<String, String> metadata) {
        this.service = service;
        this.id = id;
        this.host = host;
        this.port = port;
        this.status = status;
        this.metadata = metadata;
    }

    public ServiceInstance(Service service, String id, String host, Integer port, Long lastUpdateTime, String serviceVersion, String clientVersion, EndpointStatus status, Object origin, Map<String, String> metadata, Map<String, String> tags) {
        this.service = service;
        this.id = id;
        this.host = host;
        this.port = port;
        this.lastUpdateTime = lastUpdateTime;
        this.serviceVersion = serviceVersion;
        this.clientVersion = clientVersion;
        this.status = status;
        this.origin = origin;
        this.metadata = metadata;
        this.tags = tags;
    }

    public ServiceInstance(Service service, String id, String host, Integer port, Long lastUpdateTime, String serviceVersion, String clientVersion, EndpointStatus status, Object origin, Map<String, String> metadata, Map<String, String> tags, Boolean heartBeat, Integer ttl) {
        this.service = service;
        this.id = id;
        this.host = host;
        this.port = port;
        this.lastUpdateTime = lastUpdateTime;
        this.serviceVersion = serviceVersion;
        this.clientVersion = clientVersion;
        this.status = status;
        this.origin = origin;
        this.metadata = metadata;
        this.tags = tags;
        this.heartBeat = heartBeat;
        this.ttl = ttl;
    }

    public ServiceInstance(String id, String host, Integer port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

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

    public String getAddr() {
        return host + ":" + port;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHostPort() {
        return this.host + ":" + this.port;
    }

    public EndpointStatus getStatus() {
        return status;
    }

    public void setStatus(EndpointStatus status) {
        this.status = status;
    }

    public String getMetadata(String key) {
        return metadata.get(key);
    }

    public void setMetadata(String key, String val) {
        metadata.put(key, val);
    }

    public Map<String, String> getAllMetadata() {
        return metadata;
    }

    public void setAllMetadata(Map<String, String> maps) {
        this.metadata = maps;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void setTag(String key, String value) {
        tags.put(key, value);
    }

    public Service getService() {
        return this.service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    public Boolean getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(Boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return this.host + ":" + this.port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceInstance)) {
            return false;
        }
        ServiceInstance that = (ServiceInstance) o;
        return Objects.equals(service, that.service) &&
                host.equals(that.host) &&
                port.equals(that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, host, port);
    }
}
