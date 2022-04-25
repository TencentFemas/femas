package com.tencent.tsf.femas.entity.rule.limit;

import com.tencent.tsf.femas.entity.ServiceModel;
import io.swagger.annotations.ApiModelProperty;

public class LimitModel extends ServiceModel {

    @ApiModelProperty("类型 全局限流：GLOBAL，局部限流： PART")
    public String type;

    @ApiModelProperty("规则名搜索")
    private String keyword;

    public LimitModel() {
    }

    public LimitModel(String namespaceId, String serviceName) {
        super(namespaceId, serviceName);
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
