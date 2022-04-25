/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.service.trace.skywalking;

import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.BACKEND_ADDR;
import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.endPointPath.GET_ALL_SERVICES;
import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.endPointPath.GET_SERVICES_TOPOLOGY;
import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.endPointPath.QUERY_BASIC_TRACES;
import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.endPointPath.QUERY_TRACE;
import static com.tencent.tsf.femas.entity.trace.skywalking.SkywalkingConstant.endPointPath.READ_METRICS_VALUES;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;
import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.entity.namespace.Namespace;
import com.tencent.tsf.femas.entity.registry.RegistryPageService;
import com.tencent.tsf.femas.entity.registry.ServiceBriefInfo;
import com.tencent.tsf.femas.entity.trace.NodeMetricsQueryCondition;
import com.tencent.tsf.femas.entity.trace.TopologyQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceDetailQueryCondition;
import com.tencent.tsf.femas.entity.trace.TraceQueryCondition;
import com.tencent.tsf.femas.entity.trace.config.SkyWalkingConditional;
import com.tencent.tsf.femas.entity.trace.skywalking.BasicTrace;
import com.tencent.tsf.femas.entity.trace.skywalking.Duration;
import com.tencent.tsf.femas.entity.trace.skywalking.KeyValue;
import com.tencent.tsf.femas.entity.trace.skywalking.MicroServiceType;
import com.tencent.tsf.femas.entity.trace.skywalking.Node;
import com.tencent.tsf.femas.entity.trace.skywalking.Pagination;
import com.tencent.tsf.femas.entity.trace.skywalking.Ref;
import com.tencent.tsf.femas.entity.trace.skywalking.RefType;
import com.tencent.tsf.femas.entity.trace.skywalking.Scope;
import com.tencent.tsf.femas.entity.trace.skywalking.Service;
import com.tencent.tsf.femas.entity.trace.skywalking.SkyWalkingMetricsType;
import com.tencent.tsf.femas.entity.trace.skywalking.Span;
import com.tencent.tsf.femas.entity.trace.skywalking.Step;
import com.tencent.tsf.femas.entity.trace.skywalking.Topology;
import com.tencent.tsf.femas.entity.trace.skywalking.Trace;
import com.tencent.tsf.femas.entity.trace.skywalking.TraceBrief;
import com.tencent.tsf.femas.entity.trace.vo.BasicTraceVo;
import com.tencent.tsf.femas.entity.trace.vo.NodeVo;
import com.tencent.tsf.femas.entity.trace.vo.SpanVo;
import com.tencent.tsf.femas.entity.trace.vo.TopologyVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceBriefVo;
import com.tencent.tsf.femas.entity.trace.vo.TraceVo;
import com.tencent.tsf.femas.service.registry.RegistryManagerService;
import com.tencent.tsf.femas.service.trace.OpentracingServer;
import com.tencent.tsf.femas.storage.DataOperation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.tencent.tsf.femas.util.MapUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mountcloud.graphql.GraphqlClient;
import org.mountcloud.graphql.request.param.RequestParameter;
import org.mountcloud.graphql.request.query.DefaultGraphqlQuery;
import org.mountcloud.graphql.request.query.GraphqlQuery;
import org.mountcloud.graphql.request.result.ResultAttributtes;
import org.mountcloud.graphql.response.GraphqlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/11 17:23
 */
@Component
@Conditional(SkyWalkingConditional.class)
public class SkyWalkingTracingServer implements OpentracingServer {

    private static final Logger log = LoggerFactory.getLogger(SkyWalkingTracingServer.class);

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<String, String> serviceNameMap = new ConcurrentHashMap<String, String>();
    private final DataOperation dataOperation;
    private final ExecutorService executorService;
    private final RegistryManagerService registryManagerService;
    private final Environment env;

    public SkyWalkingTracingServer(DataOperation dataOperation, ExecutorService executorService,
            RegistryManagerService registryManagerService, Environment env) {
        this.dataOperation = dataOperation;
        this.executorService = executorService;
        this.registryManagerService = registryManagerService;
        this.env = env;
    }

    private List<Service> getAllServices(Duration queryDuration) {
        GraphqlClient graphqlClient = GraphqlClient.buildGraphqlClient(getServerAddr());
        Map<String, String> httpHeaders = new HashMap<>();
        graphqlClient.setHttpHeaders(httpHeaders);
        GraphqlQuery query = new DefaultGraphqlQuery(GET_ALL_SERVICES);
        RequestParameter requestParameter = query.getRequestParameter();
        Map<String, Object> duration = new HashMap<>();
        duration.put("start", queryDuration.getStartTimeBucket());
        duration.put("end", queryDuration.getEndTimeBucket());
        duration.put("step", Step.MINUTE);
        requestParameter.addObjectParameter("duration", duration);
        query.addResultAttributes("key: id", "label: name", "group");
        try {
            GraphqlResponse response = graphqlClient.doQuery(query);
            Map res = response.getData();
            if (res.get("errors") != null) {
                log.error("graphql response error: " + res.get("errors"));
                return null;
            }
            Map result = (Map) res.get("data");
            if (MapUtils.isEmpty(result)) {
                log.warn("graphql response is empty");
                return null;
            }
            List<Service> data = mapper.readValue(mapper.writeValueAsString(result.get(GET_ALL_SERVICES)),
                    new TypeReference<List<Service>>() {
                    });
            return data;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    private List<String> mergeService(List<ServiceBriefInfo> briefInfos, List<Service> services) {
        List<String> strings = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(services)) {
            services.stream().forEach(s -> {
                serviceNameMap.put(s.getLabel(), s.getKey());
                if (CollectionUtils.isNotEmpty(briefInfos)) {
                    briefInfos.stream().forEach(b -> {
                        if (s.getLabel().equalsIgnoreCase(b.getServiceName())) {
                            strings.add(s.getKey());
                        }
                    });
                }
            });

        }
        return strings;
    }

    @Override
    public TopologyVo getServiceTopology(TopologyQueryCondition condition) {
        List<ServiceBriefInfo> briefInfos = getServiceByNamespaceId(condition.getNamespaceId());
        if (CollectionUtils.isNotEmpty(briefInfos)) {
            if (!condition.getQueryDuration().validate()) {
                throw new FemasRuntimeException("queryDuration error");
            }
            try {
                Namespace namespace = dataOperation.fetchNamespaceById(condition.getNamespaceId());
                if (namespace == null) {
                    throw new FemasRuntimeException("illegal namespace");
                }
                List<Service> services = getAllServices(condition.getQueryDuration());
                Duration duration = condition.getQueryDuration();
                GraphqlClient graphqlClient = GraphqlClient.buildGraphqlClient(getServerAddr());
                Map<String, String> httpHeaders = new HashMap<>();
                graphqlClient.setHttpHeaders(httpHeaders);
                GraphqlQuery query = new DefaultGraphqlQuery(GET_SERVICES_TOPOLOGY);
                Map<String, Object> durationMap = new HashMap<>();
                durationMap.put("start", duration.getStartTimeBucket());
                durationMap.put("end", duration.getEndTimeBucket());
                durationMap.put("step", Step.MINUTE);
                query.getRequestParameter()
                        .addObjectParameter("duration", durationMap);//graphqlRequest 是graphql定义的方法getTransData 的参数
                query.getRequestParameter().addObjectParameter("serviceIds",
                        mergeService(briefInfos, services));//graphqlRequest 是graphql定义的方法getTransData 的参数

                ResultAttributtes nodes = new ResultAttributtes("nodes");
                nodes.addResultAttributes("id", "name", "type", "isReal");
                ResultAttributtes clalls = new ResultAttributtes("calls");
                clalls.addResultAttributes("id", "source", "detectPoints", "target");
                query.addResultAttributes(nodes);
                query.addResultAttributes(clalls);
                //执行query
                GraphqlResponse response = graphqlClient.doQuery(query);
                //获取数据，数据为map类型
                Map res = response.getData();
                if (res.get("errors") != null) {
                    log.error("graphql response error: " + res.get("errors"));
                    return null;
                }
                Map result = (Map) res.get("data");
                if (MapUtils.isEmpty(result)) {
                    log.warn("graphql response is empty");
                    return null;
                }
                Map data = (Map) result.get(GET_SERVICES_TOPOLOGY);
                Topology topology = null;
                try {
                    topology = mapper.readValue(mapper.writeValueAsString(data), Topology.class);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage(), e);
                }
                List<Node> tNodes = Optional.of(topology).map(t -> t.getNodes()).orElse(Collections.emptyList());
                List<NodeVo> voList = new ArrayList<NodeVo>();
                if (CollectionUtils.isNotEmpty(tNodes)) {
                    tNodes.stream().forEach(t -> {
                        NodeVo vo = new NodeVo();
                        BeanUtils.copyProperties(t, vo);
                        vo.setNamespaceId(namespace.getNamespaceId());
                        vo.setNamespaceName(namespace.getName());
                        vo.setRegistryId(namespace.getRegistryId().get(0));
                        for (MicroServiceType microServiceType : MicroServiceType.values()) {
                            String type = t.getType();
                            if (StringUtils.isNotBlank(type) && type.equalsIgnoreCase(microServiceType.name())) {
                                vo.setType("microService");
                            }
                        }
                        vo.setErrorRate(queryErrorRate(
                                new NodeMetricsQueryCondition(NodeMetricsQueryCondition.MetricsType.ERROR_RATE,
                                        t.getName(), duration)));
                        vo.setAverageDuration(queryAvgTime(
                                new NodeMetricsQueryCondition(NodeMetricsQueryCondition.MetricsType.AVG_TIME,
                                        t.getName(), duration)));
                        vo.setRequestVolume(queryRequestVolume(
                                new NodeMetricsQueryCondition(NodeMetricsQueryCondition.MetricsType.REQUEST_VOLUME,
                                        t.getName(), duration)));
                        voList.add(vo);
                    });
                }
                TopologyVo topologyVo = new TopologyVo(voList,
                        Optional.of(topology).map(t -> t.getCalls()).orElse(Collections.emptyList()));
                return topologyVo;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public TraceBriefVo queryNamespaceBasicTraces(TraceQueryCondition condition) {
        if (Optional.of(condition).map(c -> c.getServiceName()).isPresent()) {
            return queryBasicTraces(condition);
        }
        List<ServiceBriefInfo> serviceBriefInfos = getServiceByNamespaceId(condition.getNamespaceId());
        if (CollectionUtils.isEmpty(serviceBriefInfos)) {
            return new TraceBriefVo(0);
        }
        final AtomicInteger total = new AtomicInteger();
        final List<BasicTraceVo> traces = new ArrayList<>();
        final List<FutureTask<Void>> tasks = new ArrayList<>();
        serviceBriefInfos.stream().forEach(s -> {
            FutureTask<Void> task = new FutureTask<Void>(() -> {
                final TraceQueryCondition loop = new TraceQueryCondition();
                BeanUtils.copyProperties(condition, loop);
                loop.setServiceName(s.getServiceName());
                TraceBriefVo tb = queryBasicTraces(loop);
                if (tb != null && CollectionUtils.isNotEmpty(tb.getTraces())) {
                    total.addAndGet(tb.getTotal());
                    traces.addAll(tb.getTraces());
                }
                return null;
            });
            executorService.submit(task);
            tasks.add(task);
        });
        tasks.stream().forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException e) {
                log.error("queryNamespaceBasicTraces  failed  ", e);
            } catch (ExecutionException e) {
                log.error("queryNamespaceBasicTraces  failed  ", e);
            }
        });
        traces.subList(0, condition.getPaging().getPageSize());
        return new TraceBriefVo(traces, total.get());
    }

    List<ServiceBriefInfo> getServiceByNamespaceId(String namespaceId) {
        Result<RegistryPageService> pageService = registryManagerService
                .describeRegisterService(namespaceId, null, 1, Integer.MAX_VALUE, null);
        if (pageService.getCode().equalsIgnoreCase(Result.SUCCESS) && Optional.of(pageService.getData())
                .map(s -> s.getServiceBriefInfos()).isPresent()) {
            RegistryPageService registryPageService = pageService.getData();
            return registryPageService.getServiceBriefInfos();
        }
        return Collections.emptyList();
    }


    @Override
    public TraceBriefVo queryBasicTraces(TraceQueryCondition condition) {
        condition.setDefault();
        try {
            Namespace namespace = dataOperation.fetchNamespaceById(condition.getNamespaceId());
            GraphqlClient graphqlClient = GraphqlClient.buildGraphqlClient(getServerAddr());
            Map<String, String> httpHeaders = new HashMap<>();
            graphqlClient.setHttpHeaders(httpHeaders);
            GraphqlQuery query = new DefaultGraphqlQuery(QUERY_BASIC_TRACES);
            Map<String, Object> request = new HashMap<>();
            Duration duration = condition.getQueryDuration();
            Map<String, Object> durationMap = new HashMap<>();
            durationMap.put("start", duration.getStartTimeBucket());
            durationMap.put("end", duration.getEndTimeBucket());
            durationMap.put("step", Step.MINUTE);
            Pagination pagination = condition.getPaging();
            request.put("queryDuration", durationMap);

            if (condition.getMinTraceDuration() != null) {
                request.put("minTraceDuration", condition.getMinTraceDuration());
            }
            if (condition.getMaxTraceDuration() != null) {
                request.put("maxTraceDuration", condition.getMaxTraceDuration());
            }

            if (!StringUtils.isEmpty(condition.getEndpointName())) {
                request.put("endpointName", condition.getEndpointName());
            }

            if (!StringUtils.isEmpty(condition.getServiceName()) && StringUtils
                    .isNotEmpty(serviceNameMap.get(condition.getServiceName()))) {
                request.put("serviceId", serviceNameMap.get(condition.getServiceName()));
            }

            if (!StringUtils.isEmpty(condition.getTraceId())) {
                request.put("traceId", condition.getTraceId());
            }

            Map<String, Object> paging = new HashMap<>();
            paging.put("pageNum", pagination.getPageNum());
            paging.put("pageSize", pagination.getPageSize());
            paging.put("needTotal", true);
            request.put("paging", paging);
            request.put("traceState", condition.getTraceState());
            request.put("queryOrder", condition.getQueryOrder());
            RequestParameter requestParameter = query.getRequestParameter();
            requestParameter.addObjectParameter("condition", request);//graphqlRequest 是graphql定义的方法getTransData 的参数
            ResultAttributtes traces = new ResultAttributtes("traces");
            traces.addResultAttributes("key: segmentId", "endpointNames", "duration", "start", "isError", "traceIds");
            query.addResultAttributes(traces);
            query.addResultAttributes("total");
            GraphqlResponse response = graphqlClient.doQuery(query);
            Map res = response.getData();
            if (res.get("errors") != null) {
                log.error("graphql response error: " + res.get("errors"));
                return null;
            }
            Map result = (Map) res.get("data");
            if (MapUtils.isEmpty(result)) {
                log.warn("graphql response is empty");
                return null;
            }
            Map data = (Map) result.get(QUERY_BASIC_TRACES);
            TraceBrief basicTrace = mapper.readValue(mapper.writeValueAsString(data), TraceBrief.class);
            List<BasicTrace> basicTraces = Optional.of(basicTrace).map(t -> t.getTraces())
                    .orElse(Collections.EMPTY_LIST);
            List<BasicTraceVo> tracesVo = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(basicTraces)) {
                basicTraces.stream().forEach(t -> {
                    BasicTraceVo vo = new BasicTraceVo();
                    String traceId = t.getTraceIds().get(0);
                    TraceVo traceVo = queryTrace(new TraceDetailQueryCondition(traceId));
                    if (traceVo != null && CollectionUtils.isNotEmpty(traceVo.getSpans())) {
                        SpanVo spanVo = traceVo.getSpans().get(0);
                        vo.setEntryService(spanVo.getServiceCode());
                    }
                    BeanUtils.copyProperties(t, vo);
                    vo.setTraceId(traceId);
                    vo.setNamespaceId(namespace.getNamespaceId());
                    vo.setNamespaceName(namespace.getName());
                    vo.setRegistryId(namespace.getRegistryId().get(0));
                    tracesVo.add(vo);
                });
            }
            TraceBriefVo traceBriefVo = new TraceBriefVo(tracesVo, basicTrace.getTotal());
            return traceBriefVo;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public TraceVo queryTrace(TraceDetailQueryCondition condition) {

        GraphqlClient graphqlClient = GraphqlClient.buildGraphqlClient(getServerAddr());
        Map<String, String> httpHeaders = new HashMap<>();
        graphqlClient.setHttpHeaders(httpHeaders);
        GraphqlQuery query = new DefaultGraphqlQuery(QUERY_TRACE);
        RequestParameter requestParameter = query.getRequestParameter();
        requestParameter.addParameter("traceId", condition.getTraceId());//graphqlRequest 是graphql定义的方法getTransData 的参数

        ResultAttributtes trace = new ResultAttributtes("trace");
        ResultAttributtes span = new ResultAttributtes("spans");
        span.addResultAttributes("traceId", "segmentId", "spanId", "parentSpanId", "serviceCode", "serviceInstanceName",
                "startTime", "endTime", "endpointName", "type", "peer", "component", "isError", "layer");
        ResultAttributtes refs = new ResultAttributtes("refs");
        refs.addResultAttributes("traceId", "parentSegmentId", "parentSpanId", "type");
        ResultAttributtes tags = new ResultAttributtes("tags");
        tags.addResultAttributes("key", "value");
        ResultAttributtes logs = new ResultAttributtes("logs");
        logs.addResultAttributes("time");
        ResultAttributtes data = new ResultAttributtes("data");
        data.addResultAttributes("key", "value");
        logs.addResultAttributes(data);

        span.addResultAttributes(refs);
        span.addResultAttributes(tags);
        span.addResultAttributes(logs);
        trace.addResultAttributes(span);

        query.addResultAttributes(span);

        try {
            GraphqlResponse response = graphqlClient.doQuery(query);
            Map res = response.getData();
            if (res.get("errors") != null) {
                log.error("graphql response error: " + res.get("errors"));
                return null;
            }
            Map result = (Map) res.get("data");
            if (MapUtils.isEmpty(result)) {
                log.warn("graphql response is empty");
                return null;
            }
            Map resData = (Map) result.get(QUERY_TRACE);
            Trace basicTrace = mapper.readValue(mapper.writeValueAsString(resData), Trace.class);
            List<SpanVo> spanVos = new ArrayList<SpanVo>();
            List<Span> spanList = Optional.ofNullable(basicTrace).map(t -> t.getSpans())
                    .orElse(Collections.emptyList());
            final AtomicInteger index = new AtomicInteger(0);
            if (CollectionUtils.isNotEmpty(spanList)) {
                spanList.stream().forEach(s -> {
                    SpanVo spanVo = new SpanVo();
                    BeanUtils.copyProperties(s, spanVo);
                    buildSysTags(spanVo);
                    SpanVo extractSpan = null;
                    if (checkNeedExtraSpan(spanList, index.get())) {
                        extractSpan = buildEntrySpan(s);
                    }
                    spanVo.setDuration(spanVo.getEndTime() - spanVo.getStartTime());
                    spanVo.verify();
                    spanVos.add(spanVo);
                    if (extractSpan != null) {
                        spanVos.add(extractSpan);
                    }
                    index.incrementAndGet();
                });
            }
            TraceVo traceVo = new TraceVo(spanVos);
            traceVo.buildTraceRefs();
            return traceVo;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 解决下游没有接探针，没有entry span无问题
     *
     * @param spanList
     * @param index
     * @return
     */
    private boolean checkNeedExtraSpan(List<Span> spanList, int index) {
        Span span = spanList.get(index);
        if (isClient(span) && isRpc(span)) {
            Span next = null;
            try {
                next = spanList.get(++index);
            } catch (IndexOutOfBoundsException e) {
            }
            if (next != null &&
                    isServer(next) && isRpc(next)) {
                return false;
            }
        }
        return true;
    }

    boolean isClient(Span span) {
        return span.getType().equalsIgnoreCase("exit");
    }

    boolean isServer(Span span) {
        boolean isServer = span.getType().equalsIgnoreCase("Entry");
        return isServer;
    }

    boolean isRpc(Span span) {
        boolean isRpc = span.getLayer().equalsIgnoreCase("http") || span.getLayer().equalsIgnoreCase("RPCFramework");
        return isRpc;
    }

    private SpanVo buildEntrySpan(Span span) {
        SpanVo vo = new SpanVo();
        vo.setType("Entry");
        vo.setParentSpanId(-1);
        vo.setEndpointName(span.getEndpointName());
        vo.setLayer("Extra");
        vo.setIsError(span.getIsError());
        vo.setLocalIp(span.getPeer());
        List<Ref> refs = new ArrayList<>();
        Ref ref = new Ref(span.getTraceId(), span.getSegmentId(), span.getSpanId(), RefType.CROSS_PROCESS);
        refs.add(ref);
        vo.setRefs(refs);
        return vo;
    }

    private void buildSysTags(SpanVo spanVo) {
        List<KeyValue> sysTags = new ArrayList<KeyValue>();
        sysTags.add(new KeyValue("__local.component", spanVo.getComponent()));
        sysTags.add(new KeyValue("__peer.ip", spanVo.getPeer()));
        sysTags.add(new KeyValue("__kind", spanVo.getType()));
        sysTags.add(new KeyValue("__local.service", spanVo.getServiceCode()));
        sysTags.add(new KeyValue("__local.operation", spanVo.getEndpointName()));
        spanVo.getTags().addAll(sysTags);
    }

    private List<KeyValue> readMetricsValues(NodeMetricsQueryCondition queryCondition) {
        try {
            GraphqlClient graphqlClient = GraphqlClient.buildGraphqlClient(getServerAddr());
            Map<String, String> httpHeaders = new HashMap<>();
            graphqlClient.setHttpHeaders(httpHeaders);
            GraphqlQuery query = new DefaultGraphqlQuery(READ_METRICS_VALUES);
            RequestParameter requestParameter = query.getRequestParameter();
            Map<String, Object> durationMap = new HashMap<>();
            durationMap.put("start", queryCondition.getQueryDuration().getStartTimeBucket());
            durationMap.put("end", queryCondition.getQueryDuration().getEndTimeBucket());
            durationMap.put("step", Step.MINUTE);
            requestParameter.addObjectParameter("duration", durationMap);

            Map<String, Object> condition = new HashMap<>();
            Map<String, Object> entity = new HashMap<>();
            entity.put("scope", Scope.Service);
            entity.put("serviceName", queryCondition.getServiceName());
            entity.put("normal", true);
            if (SkyWalkingMetricsType.getTypeByCondition(queryCondition.getMetricsName()) != null) {
                condition.put("name", SkyWalkingMetricsType.getTypeByCondition(queryCondition.getMetricsName()).name());
            }
            condition.put("entity", entity);
            requestParameter.addObjectParameter("condition", condition);

            ResultAttributtes metricsValues = new ResultAttributtes("values");
            metricsValues.addResultAttributes("values {value}");
            ResultAttributtes label = new ResultAttributtes("label");
            query.addResultAttributes(label);
            query.addResultAttributes(metricsValues);
            GraphqlResponse response = graphqlClient.doQuery(query);
            Map res = response.getData();
            if (res.get("errors") != null) {
                log.error("graphql response error: " + res.get("errors"));
                return Collections.emptyList();
            }
            Map result = (Map) res.get("data");
            if (MapUtils.isEmpty(result)) {
                log.warn("graphql response is empty");
                return Collections.emptyList();
            }
            Map resData = (Map) result.get(READ_METRICS_VALUES);
            Map values = (Map) resData.get("values");
            List<KeyValue> data = mapper
                    .readValue(mapper.writeValueAsString(values.get("values")), new TypeReference<List<KeyValue>>() {});
            return data;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public Long queryRequestVolume(NodeMetricsQueryCondition condition) {
        final AtomicLong volume = new AtomicLong(0);
        try {
            List<KeyValue> data = readMetricsValues(condition);
            if (CollectionUtils.isNotEmpty(data)) {
                data.stream().forEach(t -> {
                    volume.addAndGet(NumberUtils.toLong(t.getValue()) * 86400);
                });
            }
        } catch (Exception e) {
            log.error("queryRequestVolume failed", e);
        }
        return volume.get();
    }

    @Override
    public Double queryErrorRate(NodeMetricsQueryCondition condition) {

        final AtomicDouble sucRate = new AtomicDouble(0);
        try {
            List<KeyValue> data = readMetricsValues(condition);
            condition.setMetricsName(NodeMetricsQueryCondition.MetricsType.REQUEST_VOLUME);
            Long summary = queryRequestVolume(condition);
            if (summary.equals(0L)) {
                return 0.0d;
            }
            List<KeyValue> requestData = readMetricsValues(condition);
            AtomicDouble sucVolume = new AtomicDouble();
            for (int index = 0; index < data.size(); index++) {
                sucVolume.addAndGet((NumberUtils.toLong(data.get(index).getValue()) * 86400) * (NumberUtils
                        .toLong(requestData.get(index).getValue())));
            }
            Double rate = sucVolume.get() / summary / 10000;
            if (rate > 0) {
                return 1 - rate;
            }
        } catch (Exception e) {
            log.error("querySuccessRate failed", e);
        }
        return sucRate.get();
    }

    @Override
    public Double queryAvgTime(NodeMetricsQueryCondition condition) {
        final AtomicDouble avgTime = new AtomicDouble(0);
        try {
            List<KeyValue> data = readMetricsValues(condition);
            condition.setMetricsName(NodeMetricsQueryCondition.MetricsType.REQUEST_VOLUME);
            Long summary = queryRequestVolume(condition);
            if (summary.equals(0L)) {
                return 0.0d;
            }
            List<KeyValue> requestData = readMetricsValues(condition);
            AtomicDouble sumTime = new AtomicDouble();
            for (int index = 0; index < data.size(); index++) {
                sumTime.addAndGet((NumberUtils.toLong(data.get(index).getValue()) * 86400) * (NumberUtils
                        .toLong(requestData.get(index).getValue())));
            }
            return sumTime.get() / summary;
        } catch (Exception e) {
            log.error("queryAvgTime failed", e);
        }
        return avgTime.get();
    }

    @Override
    public String getServerAddr() {
        String address = env.getProperty(BACKEND_ADDR);
        address = StringUtils.isEmpty(address) ? OpentracingServer.super.getServerAddr() : address;
        return address.concat("/graphql");
    }

}
