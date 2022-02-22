package com.tencent.tsf.femas.governance.ratelimit.entity;

import com.tencent.tsf.femas.common.tag.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cody
 * @date 2021 2021/7/20 5:47 下午
 */
public class InitLimitRule {

    private List<Tag> tags;

    private int duration;

    private int totalQuota;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTotalQuota() {
        return totalQuota;
    }

    public void setTotalQuota(int totalQuota) {
        this.totalQuota = totalQuota;
    }

    public Map toMapRule() {
        HashMap<String, Object> rule = new HashMap<>();
        ArrayList<HashMap> conditions = new ArrayList<>();
        if (tags != null) {
            this.tags.stream().forEach(s1 -> {
                HashMap<String, String> condition = new HashMap<>();
                condition.put("tagField", s1.getTagField());
                condition.put("tagOperator", s1.getTagOperator());
                condition.put("tagType", s1.getTagType());
                condition.put("tagValue", s1.getTagValue());
                conditions.add(condition);
            });
        }
        rule.put("conditions", conditions);
        rule.put("duration", this.duration);
        rule.put("id", this.id);
        rule.put("quota", totalQuota);
        return rule;
    }
}
