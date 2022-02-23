package com.tencent.tsf.femas.extension.springcloud.discovery.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FemasConsulServerList extends AbstractServerList<ConsulServer> {

    private static final int DEFAULT_WATCH_TIMEOUT = 55;

    private final ConsulClient client;

    private final ConsulDiscoveryProperties properties;

    private String serviceId;

    private long index = -1;

    public FemasConsulServerList(ConsulClient client, ConsulDiscoveryProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        this.serviceId = clientConfig.getClientName();
    }

    @Override
    public List<ConsulServer> getInitialListOfServers() {
        return getServers();
    }

    @Override
    public List<ConsulServer> getUpdatedListOfServers() {
        return getServers();
    }

    private List<ConsulServer> getServers() {
        if (this.client == null) {
            return Collections.emptyList();
        }
        String tag = getTag(); // null is ok
        Response<List<HealthService>> response = this.client.getHealthServices(
                this.serviceId, tag, this.properties.isQueryPassing(),
                createQueryParamsForClientRequest(), this.properties.getAclToken());
        index = response.getConsulIndex();
        if (response.getValue() == null || response.getValue().isEmpty()) {
            return Collections.emptyList();
        }
        return transformResponse(response.getValue());
    }

    /**
     * Transforms the response from Consul in to a list of usable {@link ConsulServer}s.
     *
     * @param healthServices the initial list of servers from Consul. Guaranteed to be
     *         non-empty list
     * @return ConsulServer instances
     * @see ConsulServer#ConsulServer(HealthService)
     */
    protected List<ConsulServer> transformResponse(List<HealthService> healthServices) {
        List<ConsulServer> servers = new ArrayList<>();
        for (HealthService service : healthServices) {
            ConsulServer server = new ConsulServer(service);
            if (server.getMetadata()
                    .containsKey(this.properties.getDefaultZoneMetadataName())) {
                server.setZone(server.getMetadata()
                        .get(this.properties.getDefaultZoneMetadataName()));
            }
            servers.add(server);
        }
        return servers;
    }

    protected QueryParams createQueryParamsForClientRequest() {
        String datacenter = getDatacenter();
        if (datacenter != null) {
            return new QueryParams(datacenter, DEFAULT_WATCH_TIMEOUT, index);
        }
        return new QueryParams(DEFAULT_WATCH_TIMEOUT, index);
    }

    protected String getTag() {
        return this.properties.getQueryTagForService(this.serviceId);
    }

    protected String getDatacenter() {
        return this.properties.getDatacenters().get(this.serviceId);
    }
}
