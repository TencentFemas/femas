package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalInterceptPluginConfig {


    @JsonProperty
    private InterceptPlugin plugin;

    public GlobalInterceptPluginConfig() {
    }


    public InterceptPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(InterceptPlugin plugin) {
        this.plugin = plugin;
    }


}
