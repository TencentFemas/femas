package com.tencent.tsf.femas.governance.context;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.header.AbstractRequestMetaUtils;

import java.util.HashMap;
import java.util.Map;

public class FemasContext extends Context {
    @Override
    public void putTags(Map<String, String> tagMap) {

    }

    @Override
    public Map<String, String> getUpstreamTags() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getCurrentTags() {
        return new HashMap<>();
    }

    @Override
    public void init(String serviceName, Integer port) {

    }

    @Override
    public String getServiceNameFromContext() {
        return "";
    }

    @Override
    public String getSourceServiceName() {
        return "";
    }

    @Override
    public void putCurrentServiceName(String currentServiceName) {

    }

    @Override
    public Map<String, String> getRegistryConfigMap() {
        return new HashMap<>();
    }

    @Override
    public String getToken() {
        return "";
    }

    @Override
    public Map<String, String> getRequestMetaSerializeTags() {
        return new HashMap<>();
    }

    @Override
    public void getSerializeTagsFromRequestMeta(AbstractRequestMetaUtils headerUtils) {

    }
}
