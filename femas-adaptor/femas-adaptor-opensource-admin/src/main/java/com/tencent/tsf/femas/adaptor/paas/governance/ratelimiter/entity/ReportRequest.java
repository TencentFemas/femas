package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReportRequest {

    @Expose
    @JsonProperty
    private List<RuleStatics> rates;

    public ReportRequest(List<RuleStatics> rates) {
        this.rates = rates;
    }

    public ReportRequest() {
    }

    @Override
    public String toString() {
        return "ReportRequest{"
                + "rates=" + rates
                + '}';
    }

    public List<RuleStatics> getRates() {
        return rates;
    }

    public void setRates(List<RuleStatics> rates) {
        this.rates = rates;
    }

    @JsonInclude
    public static class RuleStatics {

        @JsonProperty("rule_id")
        @Expose
        @SerializedName("rule_id")
        String ruleId = "";

        @JsonProperty
        @Expose
        int pass = 0;

        @JsonProperty
        @Expose
        int block = 0;

        public RuleStatics() {
        }

        public RuleStatics(String ruleId, int pass, int block) {
            this.ruleId = ruleId;
            this.pass = pass;
            this.block = block;
        }

        @Override
        public String toString() {
            return "RuleStatics{"
                    + "ruleId='" + ruleId + '\''
                    + ", pass=" + pass
                    + ", block=" + block
                    + '}';
        }
    }
}
