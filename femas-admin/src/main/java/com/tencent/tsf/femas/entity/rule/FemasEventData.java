package com.tencent.tsf.femas.entity.rule;

import com.tencent.tsf.femas.entity.service.EventTypeEnum;
import java.util.Map;

public class FemasEventData {

    private String namespaceId;

    private String serviceName;

    private EventTypeEnum eventType;

    private long occurTime;

    private String upstream;

    private String downstream;

    private String instanceId;

    private byte status;

    private Map<String, String> additionalMsg;

    public FemasEventData() {
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(long occurTime) {
        this.occurTime = occurTime;
    }

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(EventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, String> getAdditionalMsg() {
        return additionalMsg;
    }

    public void setAdditionalMsg(Map<String, String> additionalMsg) {
        this.additionalMsg = additionalMsg;
    }

    public String getUpstream() {
        return upstream;
    }

    public void setUpstream(String upstream) {
        this.upstream = upstream;
    }

    public String getDownstream() {
        return downstream;
    }

    public void setDownstream(String downstream) {
        this.downstream = downstream;
    }
}
