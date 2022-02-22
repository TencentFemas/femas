package com.tencent.tsf.femas.governance.loadbalance;

import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.entity.Request;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.constant.TagConstant;
import com.tencent.tsf.femas.governance.loadbalance.impl.TagBasedLoadbalancer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class TagBasedLoadbalancerTest {

    @Test
    public void testTagBasedLoadbalancer() {
        // 构造TagBasedLoadbalancer
        Tag tagRegionGz = new Tag();
        tagRegionGz.setTagField("region");
        tagRegionGz.setTagType(TagConstant.TYPE.SYSTEM);
        tagRegionGz.setTagValue("gz");
        tagRegionGz.setTagOperator(TagConstant.OPERATOR.EQUAL);

        Tag tagZone1 = new Tag();
        tagZone1.setTagField("zone");
        tagZone1.setTagType(TagConstant.TYPE.SYSTEM);
        tagZone1.setTagValue("1");
        tagZone1.setTagOperator(TagConstant.OPERATOR.EQUAL);

        List<Tag> tags = new ArrayList<>();
        tags.add(tagRegionGz);
        tags.add(tagZone1);

        TagBasedLoadbalancer tagBasedLoadbalancer = new TagBasedLoadbalancer(tags);
        LoadbalancerManager.update(tagBasedLoadbalancer);

        // 构造Endpoint
        Set<ServiceInstance> gz = new HashSet<>();
        Set<ServiceInstance> sh = new HashSet<>();
        Set<ServiceInstance> zone1 = new HashSet<>();
        Set<ServiceInstance> zone2 = new HashSet<>();

        Service service = new Service("default_ns", "provider-demo");

        String host = "1.1.1.1";
        for (int i = 0; i < 10; i++) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setService(service);
            serviceInstance.setHost(host);
            serviceInstance.setPort(i);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("region", "gz");
            serviceInstance.setAllMetadata(metadata);

            gz.add(serviceInstance);
        }

        host = "1.1.1.2";
        for (int i = 0; i < 10; i++) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setService(service);
            serviceInstance.setHost(host);
            serviceInstance.setPort(i);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("region", "sh");
            serviceInstance.setAllMetadata(metadata);

            sh.add(serviceInstance);
        }

        host = "1.1.1.e";
        for (int i = 0; i < 10; i++) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setService(service);
            serviceInstance.setHost(host);
            serviceInstance.setPort(i);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("zone", "1");
            serviceInstance.setAllMetadata(metadata);

            zone1.add(serviceInstance);
        }

        host = "1.1.1.4";
        for (int i = 0; i < 10; i++) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setService(service);
            serviceInstance.setHost(host);
            serviceInstance.setPort(i);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("zone", "2");
            serviceInstance.setAllMetadata(metadata);

            zone2.add(serviceInstance);
        }

        List<ServiceInstance> serviceInstances = new ArrayList<>();

        // 开始测试
        serviceInstances.addAll(gz);
        serviceInstances.addAll(sh);
        serviceInstances.addAll(zone1);
        serviceInstances.addAll(zone2);

        Request request = new Request();
        request.setTargetService(service);
        request.setTargetMethodName("hello");
        Context.getRpcInfo().setRequest(request);

        for (int i = 0; i < 1000; i++) {
            ServiceInstance instance = LoadbalancerManager.select(serviceInstances);
            Assert.assertTrue(gz.contains(instance));
        }

        serviceInstances.removeAll(gz);
        for (int i = 0; i < 1000; i++) {
            ServiceInstance instance = LoadbalancerManager.select(serviceInstances);
            Assert.assertTrue(zone1.contains(instance));
        }

        serviceInstances.removeAll(zone1);
        for (int i = 0; i < 1000; i++) {
            ServiceInstance instance = LoadbalancerManager.select(serviceInstances);
            Assert.assertTrue(sh.contains(instance) || zone2.contains(instance));
        }
    }
}
