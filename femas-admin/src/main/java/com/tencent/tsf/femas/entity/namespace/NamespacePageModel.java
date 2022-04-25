package com.tencent.tsf.femas.entity.namespace;

import com.tencent.tsf.femas.entity.Page;
import io.swagger.annotations.ApiModelProperty;

public class NamespacePageModel extends Page {

    @ApiModelProperty("注册中中心id")
    private String registryId;

    @ApiModelProperty("命名空间名称、id过滤")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
    }
}
