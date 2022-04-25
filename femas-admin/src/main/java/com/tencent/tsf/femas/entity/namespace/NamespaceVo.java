package com.tencent.tsf.femas.entity.namespace;

import com.tencent.tsf.femas.entity.registry.RegistryConfig;
import java.util.List;

/**
 * @author Cody
 * @date 2021 2021/8/18 2:04 下午
 */
public class NamespaceVo {

    private String name;
    private String namespaceId;
    private List<RegistryConfig> registry;
    private String desc;
    private Integer serviceCount;

    public static NamespaceVo build(Namespace namespace, List<RegistryConfig> registryConfigs) {
        NamespaceVo namespaceVo = new NamespaceVo();
        namespaceVo.setNamespaceId(namespace.getNamespaceId());
        namespaceVo.setName(namespace.getName());
        namespaceVo.setDesc(namespace.getDesc());
        namespaceVo.setServiceCount(namespace.getServiceCount());
        namespaceVo.setRegistry(registryConfigs);
        return namespaceVo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public List<RegistryConfig> getRegistry() {
        return registry;
    }

    public void setRegistry(List<RegistryConfig> registry) {
        this.registry = registry;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getServiceCount() {
        return serviceCount;
    }

    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
}
