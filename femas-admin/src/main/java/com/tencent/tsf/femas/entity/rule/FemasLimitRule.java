package com.tencent.tsf.femas.entity.rule;

import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import io.swagger.annotations.ApiModelProperty;
import java.util.Comparator;
import java.util.List;

public class FemasLimitRule {

    @ApiModelProperty("规则id")
    private String ruleId;

    @ApiModelProperty("命名空间")
    private String namespaceId;

    @ApiModelProperty("服务名")
    private String serviceName;

    @ApiModelProperty("规则名称")
    private String ruleName;

    @ApiModelProperty("限流粒度：GLOBAL, PART")
    private Type type;

    @ApiModelProperty("流量来源规则")
    private List<Tag> tags;

    @ApiModelProperty("单位时间")
    private int duration;

    @ApiModelProperty("请求数")
    private int totalQuota;

    @ApiModelProperty("生效状态 1：开启 0：关闭")
    private Integer status;

    @ApiModelProperty("生效时间")
    private Long updateTime;

    @ApiModelProperty("描述")
    private String desc;

    public boolean judgeStatus() {
        if (status == null) {
            return false;
        }
        return status == 1;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    // 注：都为空判定不等
    public boolean isEqualTags(FemasLimitRule femasLimitRule) {
        if (femasLimitRule == null) {
            return false;
        }
        List<Tag> targetTags = femasLimitRule.getTags();
        if (CollectionUtil.isEmpty(this.tags) || CollectionUtil.isEmpty(targetTags)
                || this.tags.size() != targetTags.size()) {
            return false;
        }
        targetTags.sort(new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        this.tags.sort(new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        for (int i = 0; i < tags.size(); i++) {
            if (!tags.get(i).equals(targetTags.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName;
    }

    public enum Type {
        GLOBAL, PART,
    }

}
