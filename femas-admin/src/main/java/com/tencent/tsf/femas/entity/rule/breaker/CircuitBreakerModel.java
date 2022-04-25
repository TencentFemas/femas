package com.tencent.tsf.femas.entity.rule.breaker;

import com.tencent.tsf.femas.entity.ServiceModel;
import io.swagger.annotations.ApiModelProperty;

public class CircuitBreakerModel extends ServiceModel {

    @ApiModelProperty("隔离级别过滤")
    private String isolationLevel;

    @ApiModelProperty("下游服务名搜索")
    private String searchWord;

    public CircuitBreakerModel() {
    }

    public CircuitBreakerModel(String namespaceId, String serviceName) {
        super(namespaceId, serviceName);
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }
}
