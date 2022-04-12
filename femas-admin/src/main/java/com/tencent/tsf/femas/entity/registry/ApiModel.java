package com.tencent.tsf.femas.entity.registry;

import com.tencent.tsf.femas.entity.ServiceModel;
import io.swagger.annotations.ApiModelProperty;

public class ApiModel extends ServiceModel {

    @ApiModelProperty("接口路径")
    private String path;

    @ApiModelProperty("接口状态 1 正常  2 异常")
    private String status;

    @ApiModelProperty("服务版本")
    private String serviceVersion;

    @ApiModelProperty("接口名称查询")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }
}
