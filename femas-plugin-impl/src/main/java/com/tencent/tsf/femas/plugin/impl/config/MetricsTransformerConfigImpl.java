package com.tencent.tsf.femas.plugin.impl.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.tsf.femas.plugin.config.PluginConfigImpl;
import com.tencent.tsf.femas.plugin.config.gov.MetricsTransformerConfig;
import com.tencent.tsf.femas.plugin.config.verify.DefaultValues;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author p_mtluo
 * @Date 2021-11-09 16:30
 * @Description metrics transformer config impl
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricsTransformerConfigImpl extends PluginConfigImpl implements MetricsTransformerConfig {

    @JsonProperty
    private String type;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public void verify() throws IllegalArgumentException {

    }

    @Override
    public void setDefault() {
        if (StringUtils.isBlank(type)) {
            type = DefaultValues.DEFAULT_METRICS_TRANSFORMER;
        }
    }

    @Override
    public String toString() {
        return "MetricsTransformerConfigImpl{" +
                "type='" + type + '\'' +
                "} " + super.toString();
    }
}
