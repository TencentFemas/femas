package com.tencent.tsf.femas.governance.loadbalance.impl;

import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.engine.TagEngine;
import com.tencent.tsf.femas.governance.loadbalance.Loadbalancer;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import java.util.ArrayList;
import java.util.List;

public class TagBasedLoadbalancer extends AbstractLoadbalancer {

    /**
     * 根据Tag选择出机器列表后的Loadbalancer
     * 默认为 RoundRobinLoadbalancer
     */
    protected Loadbalancer ref;
    /**
     * 如果命中了
     */
    protected Loadbalancer random = new RandomLoadbalancer();
    /**
     * 地域优限级tag
     * eg:
     * - region
     * - zone
     * - data center
     * <p>
     * 会依次以此tag取出机器列表
     */
    private List<Tag> priorityTags;

    public TagBasedLoadbalancer() {
    }

    public TagBasedLoadbalancer(List<Tag> priorityTags) {
        this(priorityTags, new RoundRobinLoadbalancer());
    }

    public TagBasedLoadbalancer(List<Tag> priorityTags, Loadbalancer ref) {
        this.priorityTags = priorityTags;
        this.ref = ref;
    }

    @Override
    public ServiceInstance doSelect(List<ServiceInstance> serviceInstances) {
        List<ServiceInstance> instances = new ArrayList<>();

        for (Tag tag : priorityTags) {
            for (ServiceInstance serviceInstance : serviceInstances) {
                if (TagEngine.checkTagHit(tag, serviceInstance.getAllMetadata(), serviceInstance.getTags())) {
                    instances.add(serviceInstance);
                }
            }

            if (!instances.isEmpty()) {
                break;
            }
        }

        // 如果都不满足，则交给真正的ref loadbalancer执行
        if (instances.isEmpty()) {
            return ref.select(serviceInstances);
        }

        return ref.select(instances);
    }

    public List<Tag> getPriorityTags() {
        return priorityTags;
    }

    public void setPriorityTags(List<Tag> priorityTags) {
        this.priorityTags = priorityTags;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {
//        conf.getConfig().getLoadbalancer().getPluginConfig()
    }

    @Override
    public String getName() {
        return "tagBased";
    }

    @Override
    public void destroy() {

    }
}
