package com.tencent.tsf.femas.entity.registry.eureka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

/**
 * @author Leo
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class EurekaInstance {

    private String hostName;
    private String instanceId;

    private String app;
    private String ipAddr;

    private Map<String, String> port;
    //获取注册信息的最新时间

    private Long lastUpdatedTimestamp;

    //实例的最后更新时间
    private Long lastDirtyTimestamp;

    private String status;
    private String actionType;
    private Map<String, String> metadata;

    public EurekaInstance() {
    }

    public EurekaInstance(String hostName, String instanceId, String app, String ipAddr, Map<String, String> port,
            Long lastUpdatedTimestamp, Long lastDirtyTimestamp, String status, String actionType,
            Map<String, String> metadata) {
        this.hostName = hostName;
        this.instanceId = instanceId;
        this.app = app;
        this.ipAddr = ipAddr;
        this.port = port;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.lastDirtyTimestamp = lastDirtyTimestamp;
        this.status = status;
        this.actionType = actionType;
        this.metadata = metadata;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Map<String, String> getPort() {
        return port;
    }

    public void setPort(Map<String, String> port) {
        this.port = port;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public Long getLastDirtyTimestamp() {
        return lastDirtyTimestamp;
    }

    public void setLastDirtyTimestamp(Long lastDirtyTimestamp) {
        this.lastDirtyTimestamp = lastDirtyTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
}
