package com.tencent.tsf.femas.agent.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterceptPluginConfig {


    @JsonProperty
    private InterceptPlugin plugin;

    public InterceptPluginConfig() {
    }


    public InterceptPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(InterceptPlugin plugin) {
        this.plugin = plugin;
    }


}
