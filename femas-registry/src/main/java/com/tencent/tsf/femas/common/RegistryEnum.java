package com.tencent.tsf.femas.common;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/23 20:36
 * @Version 1.0
 */
public enum RegistryEnum {
    /**
     * consul
     */
    CONSUL("CONSUL"),

    /**
     * Eureka
     */
    EUREKA("EUREKA"),

    /**
     * Nacos
     */
    NACOS("NACOS"),

    /**
     * k8s
     */
    KUBERNETES("K8S"),

    /**
     * polaris
     */
    POLARIS("POLARIS"),

    /**
     * etcd
     */
    ETCD("ETCD"),

    /**
     * zookeeper
     */
    ZOOKEEPER("ZOOKEEPER");

    /**
     * alias
     */
    private final String alias;

    RegistryEnum(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

}
