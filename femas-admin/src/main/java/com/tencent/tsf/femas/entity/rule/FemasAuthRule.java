package com.tencent.tsf.femas.entity.rule;

import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.entity.pass.auth.AuthRule;
import com.tencent.tsf.femas.entity.rule.auth.RuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;

public class FemasAuthRule {

    /**
     * 规则ID
     */
    @ApiModelProperty("规则id")
    private String ruleId;
    /**
     * 规则名称
     */
    @ApiModelProperty("规则名称")
    private String ruleName;
    /**
     * 生效状态 1开启 0 关闭
     */
    @ApiModelProperty("生效状态 1开启 0 关闭")
    private String isEnabled;

    @ApiModelProperty("规则类型")
    private RuleTypeEnum ruleType;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Long createTime;
    /**
     * 生效时间
     */
    @ApiModelProperty("生效时间")
    private Long availableTime;
    /**
     * 服务名
     */
    @ApiModelProperty("服务名")
    private String serviceName;
    /**
     * 命名空间ID
     */
    @ApiModelProperty("命名空间ID")
    private String namespaceId;
    /**
     * 标签(Tag)列表
     */
    @ApiModelProperty("标签(Tag)列表")
    private List<Tag> tags;

    /**
     * 标签(Tag)计算规则
     */
    @ApiModelProperty("标签(Tag)计算规则")
    private String tagProgram;

    @ApiModelProperty("生效对象 所有接口：ALL 指定接口 PART")
    private String target;

    @ApiModelProperty("描述")
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getTagProgram() {
        return tagProgram;
    }

    public void setTagProgram(String tagProgram) {
        this.tagProgram = tagProgram;
    }

    public Long getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(Long availableTime) {
        this.availableTime = availableTime;
    }

    public RuleTypeEnum getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleTypeEnum ruleType) {
        this.ruleType = ruleType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public AuthRule toPassRule() {
        AuthRule authRule = new AuthRule();
        authRule.setRuleId(this.ruleId);
        authRule.setCreateTime(new Date(this.getCreateTime()).toString());
        authRule.setUpdateTime(new Date(this.availableTime).toString());
        authRule.setRuleName(this.getRuleName());
        authRule.setIsEnabled(this.isEnabled);
        authRule.setServiceName(this.serviceName);
        authRule.setTags(this.tags);
        authRule.setTagProgram(this.tagProgram);
        authRule.setNamespaceId(this.namespaceId);
        return authRule;
    }


    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName + "，规则状态：" + ((
                "0".equalsIgnoreCase(this.isEnabled) ? "关闭" : "开启"));
    }
}
