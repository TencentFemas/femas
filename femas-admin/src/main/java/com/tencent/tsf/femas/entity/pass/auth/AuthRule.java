package com.tencent.tsf.femas.entity.pass.auth;

import com.tencent.tsf.femas.common.tag.Tag;
import java.io.Serializable;
import java.util.List;

/**
 * TSF微服务鉴权规则
 *
 * @author hongweizhu
 */
public class AuthRule implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4319194985653367847L;
    /**
     * 规则ID
     */
    private String ruleId;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 是否启用：0：不启用；1：启用
     */
    private String isEnabled;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 服务名
     */
    private String serviceName;
    /**
     * 命名空间ID
     */
    private String namespaceId;
    /**
     * 标签(Tag)列表
     */
    private List<Tag> tags;

    /**
     * 标签(Tag)计算规则
     */
    private String tagProgram;

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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
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

    /**
     * 推送Consul前清理rule
     */
    public void clearAuthRule() {
        this.serviceName = null;
        this.namespaceId = null;
        this.createTime = null;
        this.updateTime = null;
        this.isEnabled = null;
    }

    @Override
    public String toString() {
        return "命名空间：" + namespaceId + "，服务：" + serviceName + "，规则：" + ruleName + "，规则状态：" + (isEnabled.equals("0")
                ? "关闭" : "开启");
    }


}
