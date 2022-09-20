package com.tencent.tsf.femas.adaptor.paas.governance.lane;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tencent.tsf.femas.common.constant.FemasConstant;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.ContextConstant;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.governance.lane.entity.LaneRule;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.tag.engine.TagEngine;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.governance.lane.LaneFilter;
import com.tencent.tsf.femas.plugin.context.ConfigContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.tencent.tsf.femas.common.constant.FemasConstant.FEMAS_APPLICATION_VERSION;

public class FemasLaneFilter implements LaneFilter {

    public static final String SOURCE_LANE_ID_TAG = "source.lane.id";
    private static final Logger LOGGER = LoggerFactory.getLogger(FemasLaneFilter.class);

    private static volatile Set<LaneRule> ALL_LANE_RULES = Sets.newConcurrentHashSet();

    /**
     * 实际生效的规则，按照权重排序
     * <p>
     * 规则生效且对应的泳道的入口服务为自身
     */
    private static volatile Set<LaneRule> EFFECTIVE_LANE_RULES_SET = Sets.newConcurrentHashSet();

    private static volatile List<LaneRule> EFFECTIVE_LANE_RULES = new CopyOnWriteArrayList<>();

    /**
     * 泳道中包含当前部署组且为入口的泳道
     */
    private static volatile Set<LaneInfo> EFFECTIVE_LANE_INFOS = Sets.newConcurrentHashSet();

    /**
     * 所有配置的lane信息
     * key : laneId
     * value : laneInfo
     */
    private static volatile Map<String, LaneInfo> LANE_ID_LANE_INFO_MAP = new ConcurrentHashMap<>();


    private volatile static List<String> currentGroupLaneIds = null;

    // 所有泳道涉及的部署组id列表
    private volatile static Map<String, Boolean> allLaneConfiguredGroupsIds = new ConcurrentHashMap<>();
    // laneId -> 对应泳道配置过泳道的服务列表（service#namespaceId），便于快速判断当前服务是否配置过对应的泳道
    private volatile static Map<String, Set<String>> laneConfiguredNamespaceServicesMap = new ConcurrentHashMap<>();
    // laneId -> 对应泳道配置的 groupIds，便于快速判断当前节点是否属于该泳道
    private volatile static Map<String, Set<String>> laneConfiguredGroupIdsMap = new ConcurrentHashMap<>();

    /**
     * 命名空间-landIds 映射map
     */
    private static volatile Map<String, Set<String>> NAMESPACE_LANE_INFO_MAP = new ConcurrentHashMap<>();

    private volatile static ContextConstant contextConstant = ContextFactory.getContextConstantInstance();

    private static String groupId = Context.getSystemTag(contextConstant.getApplicationVersion());
    /**
     * 部署组id-landIds 映射map
     * 供colorless逻辑使用
     * 入口部署组不算有色节点
     */

    /**
     * 部署组id-landIds 映射map
     * 供color逻辑使用
     * 入口部署组算有色节点
     */
    private static volatile Map<String, Set<String>> GROUP_LANE_INFO_COLOR_MAP = new ConcurrentHashMap<>();

    private static volatile Map<String, Map<Service, Boolean>> EFFECTIVE_SERVICE_MAP = new ConcurrentHashMap<>();

    /**
     * 将有色节点选出，淘汰无色节点
     */
    private static List<ServiceInstance> chooseColorfulInstances(List<ServiceInstance> serviceInstances,
                                                                 LaneInfo laneInfo) {
        List<ServiceInstance> serverList = Lists.newArrayList();
        String laneId = laneInfo.getLaneId();

        // 走对应泳道的节点
        for (ServiceInstance instance : serviceInstances) {
            String groupId = instance.getMetadata(FemasConstant.FEMAS_META_APPLICATION_VERSION_KEY);
            String svcName = instance.getService().getName();
            Set<String> laneIds = GROUP_LANE_INFO_COLOR_MAP.get(getLaneConfiguredVersionKey(svcName, groupId));
            if (laneIds != null && laneIds.contains(laneId)) {
                serverList.add(instance);
            }
        }

        LOGGER.debug("[FEMAS LANE] Choose Colorful instances. Femas lane take effect, color service list = {}",
                serverList);
        return serverList;
    }

    /**
     * 将有色节点选择出来
     */
    private static List<ServiceInstance> chooseColorlessInstances(Service service,
                                                                  List<ServiceInstance> serviceInstances) {
        List<ServiceInstance> instances = new ArrayList<>();
        String namespaceId = service.getNamespace();

        /**
         * 该命名空间下没有配置泳道信息
         */
        if (namespaceId == null || !NAMESPACE_LANE_INFO_MAP.containsKey(namespaceId) || NAMESPACE_LANE_INFO_MAP
                .get(namespaceId).isEmpty()) {
            return serviceInstances;
        }

        Set<ServiceInstance> colorInstances = new HashSet<>();
        for (ServiceInstance instance : serviceInstances) {
            String groupId = instance.getMetadata(FemasConstant.FEMAS_META_APPLICATION_VERSION_KEY);
            String svcName = instance.getService().getName();
            Set<String> laneIds = GROUP_LANE_INFO_COLOR_MAP.get(getLaneConfiguredVersionKey(svcName, groupId));
            if (!StringUtils.isEmpty(groupId) && laneIds != null && !laneIds.isEmpty()) {
                colorInstances.add(instance);
                continue;
            }

            instances.add(instance);
        }

        if (!CollectionUtil.isEmpty(colorInstances)) {
            LOGGER.debug("[FEMAS LANE] Choose Colorless instances. lane take effect, filter color instance list = {}",
                    colorInstances);
        }

        return instances;
    }

    public static synchronized void addLaneInfo(LaneInfo laneInfo) {
        LANE_ID_LANE_INFO_MAP.put(laneInfo.getLaneId(), laneInfo);

        for (ServiceInfo serviceInfo : laneInfo.getLaneServiceList()) {
            if (serviceInfo.getVersion().equals(FemasContext.getSystemTag(FEMAS_APPLICATION_VERSION)) && serviceInfo
                    .getEntrance()) {
                EFFECTIVE_LANE_INFOS.add(laneInfo);
                refreshEffectiveLaneRule();
            }

            if (!NAMESPACE_LANE_INFO_MAP.containsKey(serviceInfo.getNamespaceId())) {
                NAMESPACE_LANE_INFO_MAP.putIfAbsent(serviceInfo.getNamespaceId(), new HashSet<>());
            }

//            /**
//             * 入口的部署组不算为有色泳道
//             */
//            if (!laneGroup.isEntrance()) {
//                NAMESPACE_LANE_INFO_MAP.get(laneGroup.getNamespaceId()).add(laneInfo.getLaneId());
//            }
            NAMESPACE_LANE_INFO_MAP.get(serviceInfo.getNamespaceId()).add(laneInfo.getLaneId());
            String serviceGroup = getLaneConfiguredVersionKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
            if (!GROUP_LANE_INFO_COLOR_MAP.containsKey(serviceGroup)) {
//                GROUP_LANE_INFO_COLORLESS_MAP.putIfAbsent(laneGroup.getGroupId(), new HashSet<>());
                GROUP_LANE_INFO_COLOR_MAP.putIfAbsent(serviceGroup, new HashSet<>());
            }
//            /**
//             * 入口的部署组不算为有色泳道
//             */
//            if (!laneGroup.isEntrance()) {
//                GROUP_LANE_INFO_COLORLESS_MAP.get(laneGroup.getGroupId()).add(laneInfo.getLaneId());
//            }
            /**
             * 对于有色节点来说，入口部署组算有色节点
             */
            GROUP_LANE_INFO_COLOR_MAP.get(serviceGroup).add(laneInfo.getLaneId());

            /**
             * 新增泳道可能会导致原来没有配置过泳道的服务变为配置过
             * 所以这里删除所有为false的Service
             */
            EFFECTIVE_SERVICE_MAP.clear();
//            Iterator<Map.Entry<Service, Boolean>> iterator = EFFECTIVE_SERVICE_MAP.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<Service, Boolean> entry = iterator.next();
//                if (entry.getValue() == false) {
//                    iterator.remove();
//                }
//            }
        }
    }

    public static synchronized void removeLaneInfo(LaneInfo laneInfo) {
        LANE_ID_LANE_INFO_MAP.remove(laneInfo.getLaneId());

        for (ServiceInfo serviceInfo : laneInfo.getLaneServiceList()) {
            /**
             * 清除生效的lane info
             */
            if (EFFECTIVE_LANE_INFOS.contains(laneInfo)) {
                EFFECTIVE_LANE_INFOS.remove(laneInfo);
                refreshEffectiveLaneRule();
            }

            if (!NAMESPACE_LANE_INFO_MAP.containsKey(serviceInfo.getNamespaceId())) {
                NAMESPACE_LANE_INFO_MAP.putIfAbsent(serviceInfo.getNamespaceId(), new HashSet<>());
            }
            NAMESPACE_LANE_INFO_MAP.get(serviceInfo.getNamespaceId()).remove(laneInfo.getLaneId());
            String serviceGroup = getLaneConfiguredVersionKey(serviceInfo.getServiceName(), serviceInfo.getVersion());

            if (!GROUP_LANE_INFO_COLOR_MAP.containsKey(serviceGroup)) {
//                GROUP_LANE_INFO_COLORLESS_MAP.putIfAbsent(laneGroup.getGroupId(), new HashSet<>());
                GROUP_LANE_INFO_COLOR_MAP.putIfAbsent(serviceGroup, new HashSet<>());
            }
//            GROUP_LANE_INFO_COLORLESS_MAP.get(laneGroup.getGroupId()).remove(laneInfo.getLaneId());
            GROUP_LANE_INFO_COLOR_MAP.get(serviceGroup).remove(laneInfo.getLaneId());

            /**
             * 删除泳道可能会导致原来配置过泳道的服务变为没有配置过
             * 所以这里删除所有为true的Service
             */
            EFFECTIVE_SERVICE_MAP.clear();
//            Iterator<Map.Entry<Service, Boolean>> iterator = EFFECTIVE_SERVICE_MAP.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<Service, Boolean> entry = iterator.next();
//                if (entry.getValue() == true) {
//                    iterator.remove();
//                }
//            }
        }
    }

    private static synchronized void refreshEffectiveLaneRule() {
        EFFECTIVE_LANE_RULES_SET.clear();
        EFFECTIVE_LANE_RULES.clear();

        if (CollectionUtil.isEmpty(EFFECTIVE_LANE_INFOS)) {
            return;
        }

        for (LaneInfo laneInfo : EFFECTIVE_LANE_INFOS) {
            for (LaneRule laneRule : ALL_LANE_RULES) {
                Map<String, Integer> relativeLane = laneRule.getRelativeLane();
                for (Map.Entry<String, Integer> entry : relativeLane.entrySet()) {
                    if (laneInfo.getLaneId().equals(entry.getKey())) {
                        EFFECTIVE_LANE_RULES_SET.add(laneRule);
                    }
                }
            }
        }

        resortLaneRule();
        LOGGER.info("EFFECTIVE LANE Rule changed. EFFECTIVE_LANE_RULES : " + EFFECTIVE_LANE_RULES);
    }

    private static synchronized void resortLaneRule() {
        List<LaneRule> laneRules = new ArrayList<>(EFFECTIVE_LANE_RULES_SET);
        EFFECTIVE_LANE_RULES = laneRules;
        Collections.sort(EFFECTIVE_LANE_RULES, new Comparator<LaneRule>() {
            @Override
            public int compare(LaneRule r1, LaneRule r2) {
                if (r1.getPriority().equals(r2.getPriority())) {
                    return Long.compare(r2.getCreateTime().getTime(), r1.getCreateTime().getTime());
                } else {
                    return r1.getPriority().compareTo(r2.getPriority());
                }
            }
        });
    }

    public static synchronized void addLaneRule(LaneRule laneRule) {
        ALL_LANE_RULES.add(laneRule);

        refreshEffectiveLaneRule();
    }

    public static synchronized void removeLaneRule(LaneRule laneRule) {
        ALL_LANE_RULES.remove(laneRule);
        EFFECTIVE_LANE_RULES_SET.remove(laneRule);
        EFFECTIVE_LANE_RULES.remove(laneRule);

        LOGGER.info("EFFECTIVE LANE Rule changed. EFFECTIVE_LANE_RULES : " + EFFECTIVE_LANE_RULES);
    }

    /**
     * 根据泳道规则删选出流量应当去的实例
     * <p>
     * 如果当前应用是入口，则会设置泳道信息到context中，入口一般为网关
     * 如果当前应用不是入口，则会从context中读取出泳道信息
     *
     * @return
     */
    @Override
    public List<ServiceInstance> filterInstancesWithLane(Service service, List<ServiceInstance> serviceInstances) {
        if (CollectionUtil.isEmpty(serviceInstances)) {
            return serviceInstances;
        }

        preProcessLaneId();

        String laneId = Context.getRpcInfo().get(FemasConstant.LANE_ID_TAG);

        if (StringUtils.isEmpty(laneId)) {
            /**
             * 走无色节点
             */
            return chooseColorlessInstances(service, serviceInstances);
        } else {
            LaneInfo laneInfo = LANE_ID_LANE_INFO_MAP.get(laneId);
            Set<String> laneConfiguredServices = getLaneConfiguredServices(laneId);
            Set<String> groupIdsSet = getLaneConfiguredGroupIds(laneId);
            // 判断当前服务是否配置过对应的泳道
            if (laneConfiguredServices != null && groupIdsSet != null) {
                Map<Service, Boolean> effectiveMap = EFFECTIVE_SERVICE_MAP.get(laneId);
                Boolean flag = null;

                if (effectiveMap != null) {
                    flag = effectiveMap.get(service);
                }

                if (flag == null) {
                    boolean configMatch = serviceInstances.stream().anyMatch(instance -> {
                        String namespaceId = instance.getMetadata(FemasConstant.FEMAS_META_NAMESPACE_ID_KEY);
                        // 目前应用和命名空间相同则认为是配置过，该判断在同名服务的场景下有缺陷。
                        return laneConfiguredServices.contains
                                (getLaneConfiguredServiceKey(instance.getService().getName(), namespaceId));
                    });

                    LOGGER.info(
                            "Femas Lane effective service changed. Service : " + service + ", configMatch : " + configMatch
                                    + ".");
                    EFFECTIVE_SERVICE_MAP.computeIfAbsent(laneId, k -> new HashMap<>());
                    EFFECTIVE_SERVICE_MAP.get(laneId).put(service, configMatch);
                    if (configMatch) {
                        return chooseColorfulInstances(serviceInstances, laneInfo);
                    }
                } else if (flag) {
                    return chooseColorfulInstances(serviceInstances, laneInfo);
                }
            }
            /**
             * 走无色节点
             */
            return chooseColorlessInstances(service, serviceInstances);
        }
    }

    public void preProcessLaneId() {
        /**
         * 入口部署组可以设置或者重新设置LaneId
         */
        for (LaneRule effectiveLaneRule : EFFECTIVE_LANE_RULES) {
            if (CollectionUtil.isEmpty(effectiveLaneRule.getTagRule().getTags()) || TagEngine.checkRuleHitByCurrentTags(effectiveLaneRule.getTagRule())) {
                Map<String, Integer> laneMap = effectiveLaneRule.getRelativeLane();
                String laneId = getLaneIdByPercentage(laneMap);
                /**
                 * 命中后需要把LaneId塞入context中
                 */
                Context.getRpcInfo().put(FemasConstant.LANE_ID_TAG, laneId);
                return;
            }
        }
        String upstreamLaneId = Context.getRpcInfo().get(SOURCE_LANE_ID_TAG);
        if (StringUtils.isNotEmpty(upstreamLaneId)) {
            Context.getRpcInfo().put(ContextConstant.LANE_ID_TAG, upstreamLaneId);
            return;
        }
    }

    private String getLaneIdByPercentage(Map<String, Integer> laneMap) {
        Map<Integer, String> reverseMap = new HashMap<>();
        List<Integer> integers = new ArrayList<>();
        laneMap.entrySet().stream().forEach(e -> {
            reverseMap.put(e.getValue(), e.getKey());
            integers.add(e.getValue());
        });
        Collections.sort(integers, new Comparator<Integer>() {
            @Override
            public int compare(Integer r1, Integer r2) {
                return r1 - r2;
            }
        });
        Random random = new Random();
        Integer index = random.nextInt(100);
        int cur = 0;
        int res = 0;
        for (int a = 0; a < integers.size(); a++) {
            cur += integers.get(a);
            if (index < cur) {
                res = integers.get(a);
            }
        }
        return reverseMap.get(res);
    }

    @Override
    public String getName() {
        return "femasLane";
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(ConfigContext conf) throws FemasRuntimeException {

    }

    @Override
    public void destroy() {

    }

    public static LaneInfo getTsfLaneInfo(String laneId) {
        return LANE_ID_LANE_INFO_MAP.get(laneId);
    }

    public static Map<String, LaneInfo> getTsfLaneInfoMap() {
        return LANE_ID_LANE_INFO_MAP;
    }

    public static List<String> getCurrentGroupLaneIds() {
        return currentGroupLaneIds;
    }

    /**
     * 从 LANE_ID_LANE_INFO_MAP 更新 currentGroupLaneId
     *
     * @return
     */
    public static void updateLaneShortCutInfo() {
        List<String> laneIds = Collections.synchronizedList(new ArrayList());
        Map<String, Boolean> groupIds = new ConcurrentHashMap<>();

        Map<String, Set<String>> tempLaneConfiguredServicesMap = new ConcurrentHashMap<>();
        Map<String, Set<String>> tempLaneConfiguredGroupIdsMap = new ConcurrentHashMap<>();

        if (StringUtils.isNotEmpty(groupId) && LANE_ID_LANE_INFO_MAP != null && LANE_ID_LANE_INFO_MAP.size() > 0) {
            for (Map.Entry<String, LaneInfo> entry : LANE_ID_LANE_INFO_MAP.entrySet()) {
                List<ServiceInfo> serviceInfos = entry.getValue().getLaneServiceList();
                Set<String> tempLaneConfiguredServices = new HashSet<>();
                Set<String> tempLaneConfiguredGroupIds = new HashSet<>();

                if (CollectionUtils.isNotEmpty(serviceInfos)) {
                    for (ServiceInfo serviceInfo : serviceInfos) {
                        // 理论上 groupId、applicationId、nsId 都不为空，但端云联调等自己注册的可能会有缺失
                        if (StringUtils.isNotEmpty(serviceInfo.getVersion())) {
                            groupIds.put(serviceInfo.getVersion(), true);
                            tempLaneConfiguredGroupIds.add(serviceInfo.getVersion());
                        }
                        if (StringUtils.isNotEmpty(serviceInfo.getNamespaceId())) {
                            tempLaneConfiguredServices.add(getLaneConfiguredServiceKey(serviceInfo.getServiceName(), serviceInfo.getNamespaceId()));
                        }

                        if (groupId.equals(serviceInfo.getVersion())) {
                            laneIds.add(entry.getKey());
                        }
                    }
                    tempLaneConfiguredServicesMap.put(entry.getKey(), tempLaneConfiguredServices);
                    tempLaneConfiguredGroupIdsMap.put(entry.getKey(), tempLaneConfiguredGroupIds);
                }
            }
        }

        currentGroupLaneIds = laneIds;
        allLaneConfiguredGroupsIds = groupIds;
        laneConfiguredNamespaceServicesMap = tempLaneConfiguredServicesMap;
        laneConfiguredGroupIdsMap = tempLaneConfiguredGroupIdsMap;
    }

    public static Set<String> getAllLaneConfiguredGroupsIds() {
        return allLaneConfiguredGroupsIds.keySet();
    }

    public static Set<String> getLaneConfiguredServices(String laneId) {
        return laneConfiguredNamespaceServicesMap.get(laneId);
    }

    public static Set<String> getLaneConfiguredGroupIds(String laneId) {
        return laneConfiguredGroupIdsMap.get(laneId);
    }

    // 根据 applicationId 和 namespaceId 判断一个服务是否配置过泳道
    public static String getLaneConfiguredServiceKey(String service, String namespaceId) {
        return String.format("%s#%s", service, namespaceId);
    }

    // 根据 service 和版本号判断一个服务是否配置过泳道
    public static String getLaneConfiguredVersionKey(String service, String version) {
        return String.format("%s#%s", service, version);
    }

}
