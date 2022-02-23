package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReportResponse {

    @JsonProperty
    @Expose
    private List<Limit> limits;

    public ReportResponse(List<Limit> limits) {
        this.limits = limits;
    }

    public ReportResponse() {
    }

    @Override
    public String toString() {
        return "ReportResponse{" + "limits=" + limits + '}';
    }

    public List<Limit> getLimits() {
        return limits;
    }

    public void setLimits(List<Limit> limits) {
        this.limits = limits;
    }

    public static class Limit {

        @JsonProperty("rule_id")
        @Expose
        @SerializedName("rule_id")
        private String ruleId = "";

        @JsonProperty("rate")
        @Expose
        @SerializedName("rate")
        private int quota = 0;

        public Limit() {
        }

        public Limit(String ruleId, int quota) {
            this.ruleId = ruleId;
            this.quota = quota;
        }

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }

        public int getQuota() {
            return quota;
        }

        public void setQuota(int quota) {
            this.quota = quota;
        }

        @Override
        public String toString() {
            return "Limit{" + "ruleId='" + ruleId + '\'' + ", quota=" + quota + '}';
        }
    }
}
