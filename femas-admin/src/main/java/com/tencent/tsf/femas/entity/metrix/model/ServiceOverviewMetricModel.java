package com.tencent.tsf.femas.entity.metrix.model;

import com.tencent.tsf.femas.entity.Page;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Cody
 * @date 2021 2021/8/15 9:41 下午
 */
public class ServiceOverviewMetricModel extends Page {

    @ApiModelProperty("命名空间id")
    private String namespaceId;

    @ApiModelProperty("开始时间")
    private Long startTime;

    @ApiModelProperty("结束时间")
    private Long endTime;

    @ApiModelProperty("排序方式 平均响应时间:avgResponseTime, 请求量:requestCount,错误率:errorRate")
    private String orderBy;

    @ApiModelProperty("服务名查询")
    private String searchKey;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }
}
