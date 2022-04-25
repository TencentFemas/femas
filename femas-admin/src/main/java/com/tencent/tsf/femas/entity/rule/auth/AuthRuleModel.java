package com.tencent.tsf.femas.entity.rule.auth;

import com.tencent.tsf.femas.entity.ServiceModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Cody
 * @date 2021 2021/9/23 7:35 下午
 */
public class AuthRuleModel extends ServiceModel {

    @ApiModelProperty("规则名搜索")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
