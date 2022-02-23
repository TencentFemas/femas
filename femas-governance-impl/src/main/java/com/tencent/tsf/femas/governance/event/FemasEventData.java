package com.tencent.tsf.femas.governance.event;


import java.util.HashMap;
import java.util.Map;

public class FemasEventData {

    private long occurTime;
    private EventTypeEnum eventType;
    private byte status;
    private String instanceId;
    private String upstream;
    private String downstream;
    private Map<String, String> additionalMsg;

    public FemasEventData() {
    }

    public static Builder custom() {
        Builder builder = new Builder();
        return builder;
    }

    public static class Builder {

        private long occurTime;
        private EventTypeEnum eventType;
        private byte status;
        private String instanceId;
        private Map<String, String> additionalMsg;
        private String upstream;
        private String downstream;

        public Builder setOccurTime(long occurTime) {
            this.occurTime = occurTime;
            return this;
        }

        public Builder setInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder setEventType(EventTypeEnum eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setStatus(byte status) {
            this.status = status;
            return this;
        }

        public Builder setUpstream(String upstream) {
            this.upstream = upstream;
            return this;
        }

        public Builder setDownstream(String downstream) {
            this.downstream = downstream;
            return this;
        }

        public Builder setAddition(String key, String value) {
            if (this.additionalMsg == null) {
                this.additionalMsg = new HashMap<>();
            }
            this.additionalMsg.put(key, value);
            return this;
        }

        public Builder setAllAdditions(Map map) {
            this.additionalMsg = map;
            return this;
        }

        public FemasEventData build() {
            FemasEventData femasEventData = new FemasEventData();
            femasEventData.eventType = this.eventType;
            femasEventData.instanceId = this.instanceId;
            femasEventData.status = this.status;
            femasEventData.occurTime = this.occurTime;
            femasEventData.additionalMsg = this.additionalMsg;
            femasEventData.upstream = this.upstream;
            femasEventData.downstream = this.downstream;
            return femasEventData;
        }
    }
}
