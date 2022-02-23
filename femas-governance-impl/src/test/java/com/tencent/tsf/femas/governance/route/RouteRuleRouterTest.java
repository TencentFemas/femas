package com.tencent.tsf.femas.governance.route;

import static com.tencent.tsf.femas.common.tag.constant.TagConstant.OPERATOR;
import static com.tencent.tsf.femas.common.tag.constant.TagConstant.TYPE;

import com.tencent.tsf.femas.common.entity.Endpoint;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.tag.Tag;
import com.tencent.tsf.femas.common.tag.TagRule;
import com.tencent.tsf.femas.governance.route.entity.RouteDest;
import com.tencent.tsf.femas.governance.route.entity.RouteRule;
import com.tencent.tsf.femas.governance.route.entity.RouteRuleGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RouteRuleRouterTest {

    private static final String NAMESPACE = "test-namespace";
    private static final String CONSUMER_SERVICE = "consumer";
    private static final String PROVIDER_SERVICE = "provider";


    /**
     * 将全部流量路由到v1和v2版本
     * v1 : 80%
     * v2 : 20%
     */
    @Test
    public void test01() {
        Service service = new Service(NAMESPACE, PROVIDER_SERVICE);

        // 构造路由规则
        RouteRuleGroup routeRuleGroup = new RouteRuleGroup();
        routeRuleGroup.setNamespace(NAMESPACE);
        routeRuleGroup.setServiceName(PROVIDER_SERVICE);

        List<RouteRule> routeRules = new ArrayList<>();
        RouteRule routeRule = new RouteRule();

        // 先测试全部流量路由规则
        List<Tag> routeTags = new ArrayList<>();
        TagRule tagRule = new TagRule();
        tagRule.setTags(routeTags);
        routeRule.setTagRule(tagRule);

        // 设置路由目的地
        List<RouteDest> routeDests = new ArrayList<>();
        RouteDest routeDest1 = new RouteDest();
        routeDest1.setDestWeight(80);
        List<Tag> destTag1 = new ArrayList<>();
        Tag destRouteTag1 = new Tag();
        destRouteTag1.setTagType(TYPE.SYSTEM);
        destRouteTag1.setTagOperator(OPERATOR.EQUAL);
        destRouteTag1.setTagField("version");
        destRouteTag1.setTagValue("v1");
        destTag1.add(destRouteTag1);
        TagRule destTagRule1 = new TagRule();
        destTagRule1.setTags(destTag1);
        routeDest1.setDestItemList(destTagRule1);
        routeDests.add(routeDest1);

        RouteDest routeDest2 = new RouteDest();
        routeDest2.setDestWeight(20);
        List<Tag> destTag2 = new ArrayList<>();
        Tag destRouteTag2 = new Tag();
        destRouteTag2.setTagType(TYPE.SYSTEM);
        destRouteTag2.setTagOperator(OPERATOR.EQUAL);
        destRouteTag2.setTagField("version");
        destRouteTag2.setTagValue("v2");
        destTag2.add(destRouteTag2);
        TagRule destTagRule2 = new TagRule();
        destTagRule2.setTags(destTag2);
        routeDest2.setDestItemList(destTagRule2);
        routeDests.add(routeDest2);

        routeRule.setDestList(routeDests);
        routeRules.add(routeRule);
        routeRuleGroup.setRuleList(routeRules);

        RouterRuleManager.refreshRouteRule(service, routeRuleGroup);

        // 初始化Router
        Router router = new FemasDefaultRouteRuleRouter();
        RouterManager.registerRouter(router);

        // 构造实例列表与访问的service
        List<ServiceInstance> serviceInstances = new ArrayList<>();

        ServiceInstance serviceInstance = new ServiceInstance();
        Endpoint endpoint = new Endpoint();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("version", "v1");
        endpoint.setMetadata(metadata);

        serviceInstance.setService(service);
        serviceInstance.setAllMetadata(metadata);
        serviceInstance.setHost("1.1.1.1");
        serviceInstance.setPort(1);
        serviceInstances.add(serviceInstance);

        ServiceInstance serviceInstance2 = new ServiceInstance();
        Endpoint endpoint2 = new Endpoint();
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("version", "v2");
        endpoint2.setMetadata(metadata2);

        serviceInstance2.setService(service);
        serviceInstance2.setAllMetadata(metadata2);
        serviceInstance2.setHost("1.1.1.2");
        serviceInstance2.setPort(1);
        serviceInstances.add(serviceInstance2);

        int version1Count = 0;
        int version2Count = 0;
        int round = 10000;
        for (int i = 0; i < round; i++) {
            Collection<ServiceInstance> serviceInstanceCollection = RouterManager.route(service, serviceInstances);

            // v1 或者 v2 都只有一台机器
            Assert.assertTrue(serviceInstanceCollection.size() == 1);
            ServiceInstance sample = serviceInstanceCollection.iterator().next();
            if (sample.getAllMetadata().get("version").equals("v1")) {
                version1Count++;
            } else if (sample.getAllMetadata().get("version").equals("v2")) {
                version2Count++;
            }
        }

        Assert.assertEquals(round, version1Count + version2Count);

        int bias = round / 10;
        double baseline1 = round * 0.8;
        double baseline2 = round * 0.2;
        System.out.println("v1 count : " + version1Count);
        System.out.println("v2 count : " + version2Count);
        Assert.assertTrue((version1Count < baseline1 + bias) && (version1Count > baseline1 - bias));
        Assert.assertTrue((version2Count < baseline2 + bias) && (version2Count > baseline2 - bias));
    }

    /**
     *
     */
    @Test
    public void test02() {

    }
}
