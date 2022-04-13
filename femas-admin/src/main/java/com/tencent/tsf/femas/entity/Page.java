package com.tencent.tsf.femas.entity;

import io.swagger.annotations.ApiModelProperty;

public class Page {

    @ApiModelProperty("页码")
    private Integer pageNo = 1;
    @ApiModelProperty("页面大小")
    private Integer pageSize = 20;


    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
