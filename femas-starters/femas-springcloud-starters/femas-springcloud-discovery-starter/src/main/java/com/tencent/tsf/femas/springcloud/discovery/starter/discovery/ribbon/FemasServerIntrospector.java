package com.tencent.tsf.femas.springcloud.discovery.starter.discovery.ribbon;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

import java.util.Map;

/**
 * 描述：
 * 创建日期：2022年05月26 00:37:17
 *
 * @author gong zhao
 **/
public class FemasServerIntrospector extends DefaultServerIntrospector {
    @Override
    public Map<String, String> getMetadata(Server server) {
        if (server instanceof FemasServer) {
            return ((FemasServer) server).getMetadata();
        }
        return super.getMetadata(server);
    }

    @Override
    public boolean isSecure(Server server) {
        if (server instanceof FemasServer) {
            return Boolean.valueOf(((FemasServer) server).getMetadata().get("secure"));
        }

        return super.isSecure(server);
    }
}
