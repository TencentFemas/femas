package com.tencent.tsf.femas.plugin.impl.config.rule.router;

import com.tencent.tsf.femas.plugin.config.gov.ServiceRouterConfig;
import com.tencent.tsf.femas.plugin.config.verify.Verifier;

import java.util.Arrays;
import java.util.Comparator;

public enum RouterType {
    RULE("FemasDefaultRoute", ServiceRouterConfig.class, 1, "femas默认规则路由");

    String name;
    Class<? extends Verifier> configClass;
    String desc;
    int index;

    RouterType(String name, Class<? extends Verifier> clazz, int index, String desc) {
        this.name = name;
        this.configClass = clazz;
        this.index = index;
        this.desc = desc;
    }

    public static RouterType getByName(String routerName) {
        for (RouterType routerType : RouterType.values()) {
            if (routerType.getName().equals(routerName)) {
                return routerType;
            }
        }
        return null;
    }

    public static Verifier getConfigByName(String routerName) {
        for (RouterType routerType : RouterType.values()) {
            if (routerType.name.equals(routerName)) {
                Verifier verifier;
                try {
                    verifier = routerType.getConfigClass().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(routerName + " create config failed.", e);
                }
                verifier.setDefault();
                return verifier;
            }
        }
        return null;
    }

    public static RouterType[] sortedValues() {
        RouterType[] routerTypes = RouterType.values();
        Arrays.sort(routerTypes, Comparator.comparingInt(RouterType::getIndex));
        return routerTypes;
    }

    public String getName() {
        return name;
    }

    public Class<? extends Verifier> getConfigClass() {
        return configClass;
    }

    public String getDesc() {
        return desc;
    }

    public int getIndex() {
        return index;
    }

}
