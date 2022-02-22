package com.tencent.tsf.femas.governance.metrics.micrometer.exporter.logger;

import com.tencent.tsf.femas.common.util.StringUtils;
import io.micrometer.core.instrument.step.StepRegistryConfig;

/**
 * @Author p_mtluo
 * @Date 2021-11-08 17:02
 * @Description LoggerRegistryConfig
 **/
public interface LoggerRegistryConfig extends StepRegistryConfig {

    LoggerRegistryConfig DEFAULT = k -> null;

    @Override
    default String prefix() {
        return "femas.metrics.export.logger";
    }

    default boolean logInactive() {
        String v = get(prefix() + ".logInactive");
        return Boolean.parseBoolean(v);
    }

    default String getFilePath() {
        String v = get(prefix() + ".filePath");
        return StringUtils.isBlank(v) ? "/data/femas_apm/monitor/logs/" : v;
    }

    default String getFileName() {
        String v = get(prefix() + ".fileName");
        return StringUtils.isBlank(v) ? "invocation_log" : v;
    }

    default Integer getFileLimit() {
        String v = get(prefix() + ".fileLimit");
        return StringUtils.isBlank(v) ? 10 * 1000 * 1000 : Integer.parseInt(v);
    }

    default Integer getFileCount() {
        String v = get(prefix() + ".fileCount");
        return StringUtils.isBlank(v) ? 10 : Integer.parseInt(v);
    }

}
