package com.tencent.tsf.femas.common;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 20:36
 * @Version 1.0
 */
public enum RegistryEnum {

    CONSUL("CONSUL"),
    EUREKA("EUREKA"),
    NACOS("NACOS"),
    KUBERNETES("K8S"),
    POLARIS("POLARIS"),
    ETCD("ETCD");

    String alias;

    RegistryEnum(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
