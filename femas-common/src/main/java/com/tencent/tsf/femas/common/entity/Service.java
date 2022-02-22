package com.tencent.tsf.femas.common.entity;

import java.util.Objects;

/**
 * 微服务实体模型
 *
 * @author zhixinzxliu
 */
public class Service {

    String namespace;
    String name;

    // for json
    public Service() {
    }

    public Service(String namespace, String name) {
        this.name = name;
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Service)) {
            return false;
        }
        Service service = (Service) o;
        return Objects.equals(namespace, service.namespace) &&
                name.equals(service.name);
    }

    @Override
    public String toString() {
        return "Service{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

}
