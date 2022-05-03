package com.tencent.tsf.femas.entity.rule;

import java.util.List;

public class FemasEventList {

    List<FemasEventData> eventData;

    public List<FemasEventData> getEventData() {
        return eventData;
    }

    public void setEventData(List<FemasEventData> eventData) {
        this.eventData = eventData;
    }

    @Override
    public String toString() {
        return "FemasEventList{" +
                "eventData=" + eventData +
                '}';
    }
}
