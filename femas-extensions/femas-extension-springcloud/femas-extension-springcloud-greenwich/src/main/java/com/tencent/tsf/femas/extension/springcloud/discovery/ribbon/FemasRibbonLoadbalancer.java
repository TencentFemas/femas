package com.tencent.tsf.femas.extension.springcloud.discovery.ribbon;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListFilter;
import com.netflix.loadbalancer.ServerListUpdater;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import java.util.List;

/**
 * @author leo
 */
public class FemasRibbonLoadbalancer<T extends Server> extends ZoneAwareLoadBalancer<T> {

    private List<FemasServiceFilterLoadBalancer> loadBalancerList;


    public FemasRibbonLoadbalancer(IClientConfig clientConfig,
            IRule rule,
            IPing ping,
            ServerList<T> serverList,
            ServerListFilter<T> filter,
            ServerListUpdater serverListUpdater,
            final List<FemasServiceFilterLoadBalancer> loadBalancerList) {
        super(clientConfig, rule, ping, serverList, filter, serverListUpdater);
        this.loadBalancerList = loadBalancerList;
    }

    @Override
    public Server chooseServer(Object key) {
        loadBalancerList.forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.beforeChooseServer(key));
        Server server = super.chooseServer(key);
        loadBalancerList
                .forEach(femasZoneAwareLoadBalancer -> femasZoneAwareLoadBalancer.afterChooseServer(server, key));
        return server;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Server> getAllServers() {
        final List<Server>[] servers = new List[]{super.getAllServers()};
        loadBalancerList.forEach(lb -> {
            servers[0] = lb.filterAllServer(servers[0]);
        });
        return servers[0];
    }

}
