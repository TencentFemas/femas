package com.tencent.tsf.femas.extension.springcloud.common.instrumentation.restapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

public final class PackageScanFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PackageScanFilter.class);

    private PackageScanFilter() {

    }

    static String scanPackage(final String configBasePackage, String splitor) {
        String validScanPackage;
        // 外部配置的扫描包
        Set<String> configPackageSet = new HashSet<>();
        if (!StringUtils.isEmpty(configBasePackage)) {
            configPackageSet.addAll(Arrays.asList(configBasePackage.split(splitor)));
        }
        Object mainClz = SwaggerContext.getAttribute(String.format("$%s", "MainClass"));
        // 有MainClass有效路径的校验
        if (mainClz != null) {
            Set<String> autoDetectPackageSet = parseDefaultScanPackage((Class<?>) mainClz);
            if (LOG.isInfoEnabled() && autoDetectPackageSet.size() > 0) {
                LOG.info("[femas swagger] auto detect default swagger scan packages: {}",
                        String.join(splitor, autoDetectPackageSet).trim());
            }
            Set<String> validScanPackageSet = merge(configPackageSet, autoDetectPackageSet);
            validScanPackage = String.join(splitor, validScanPackageSet).trim();
            if (LOG.isInfoEnabled() && !StringUtils.isEmpty(validScanPackage)) {
                LOG.info("[femas swagger] swagger scan valid packages: {}", validScanPackage);
            }
        } else {
            // 没有MainClass则使用配置的路径进行扫描。
            validScanPackage = String.join(splitor, configPackageSet);
            if (LOG.isWarnEnabled()) {
                LOG.warn("[femas swagger] cannot detect main class, swagger scanning packages is set to: {}",
                        validScanPackage);
            }
        }
        return validScanPackage;
    }

    public static Set<String> merge(Set<String> configPackageSet, Set<String> autoDetectPackageSet) {
        if (configPackageSet == null || configPackageSet.size() == 0) {
            return autoDetectPackageSet;
        }
        return configPackageSet;
    }


    private static Set<String> parseDefaultScanPackage(Class<?> mainClass) {
        Set<String> packageSets = new HashSet<>();
        try {
            SpringBootApplication bootAnnotation = mainClass.getAnnotation(SpringBootApplication.class);
            Class<?>[] baseClassPackages;
            String[] basePackages;
            String defaultPackage = mainClass.getPackage().getName();
            if (bootAnnotation == null) {
                packageSets.add(defaultPackage);
            } else {
                // baseClassPackages 注解
                baseClassPackages = bootAnnotation.scanBasePackageClasses();
                for (Class<?> clz : baseClassPackages) {
                    packageSets.add(clz.getPackage().getName());
                }
                // basePackage 注解
                basePackages = bootAnnotation.scanBasePackages();
                packageSets.addAll(Arrays.asList(basePackages));
                // 当basePackage 和 baseClassPackages 都为空时，默认使用MainClass 类所在包路径
                if (packageSets.size() == 0) {
                    packageSets.add(defaultPackage);
                }
            }
        } catch (Throwable t) {
            LOG.warn("swagger scan package is empty and auto detect main class occur exception: {}",
                    t.getMessage());
        }
        return packageSets;
    }
}
