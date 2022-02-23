package com.tencent.tsf.femas.adaptor.paas.governance.circuitbreaker.event;

public class EventResponse {

    private Integer retCode;
    private String retMsg;

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }
}
