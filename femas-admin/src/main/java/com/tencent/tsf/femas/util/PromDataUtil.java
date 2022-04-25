package com.tencent.tsf.femas.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.util.CollectionUtil;
import com.tencent.tsf.femas.entity.metrix.prom.MetricResult;
import com.tencent.tsf.femas.entity.metrix.prom.PromResponse;
import com.tencent.tsf.femas.entity.metrix.prom.VectorResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Cody
 * @date 2021 2021/8/12 10:29 下午
 *
 *         prometheus最大时间序列点不能超过11000点
 *         时间采用UTC/GMT
 */
@Component
public class PromDataUtil {

    private static volatile ObjectMapper objectMapper;
    @Value("${femas.metrics.prometheus.addr}")
    private String ADDRESS;
    private String QUERY_RANGE;
    private String QUERY;

    /**
     * 解序列化为具体PromResponse
     *
     * @param clazz
     * @param str
     * @param <T>
     * @return
     */
    public static <T> PromResponse<T> deserializeStrWithGeneric(Class<T> clazz, String str) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PromResponse.class, clazz);
        PromResponse<T> res = null;
        try {
            res = objectMapper.readValue(str, javaType);
            if (res == null) {
                throw new NullPointerException("source deserialize string is null!");
            }
            List<LinkedHashMap> result = res.getData().getResult();
            if (clazz == MetricResult.class) {
                ArrayList<MetricResult> metricResults = new ArrayList<>();
                for (LinkedHashMap linkedHashMap : result) {
                    LinkedHashMap metric = (LinkedHashMap) linkedHashMap.get("metric");
                    List<List<Object>> values = (List<List<Object>>) linkedHashMap.get("values");
                    MetricResult metricResult = new MetricResult(metric, values);
                    metricResults.add(metricResult);
                }
                res.getData().setResult(metricResults);
            } else if (clazz == VectorResult.class) {
                ArrayList<VectorResult> vectorResults = new ArrayList<>();
                for (LinkedHashMap linkedHashMap : result) {
                    LinkedHashMap metric = (LinkedHashMap) linkedHashMap.get("metric");
                    List<Object> value = (List<Object>) linkedHashMap.get("value");
                    VectorResult metricResult = new VectorResult(metric, value);
                    vectorResults.add(metricResult);
                }
                res.getData().setResult(vectorResults);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @PostConstruct
    public void init() {
        QUERY_RANGE = ADDRESS.concat("/api/v1/query_range");
        QUERY = ADDRESS.concat("/api/v1/query");
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public PromResponse<MetricResult> queryByRange(String query, Long startTime, Long endTime, Long step) {
        HashMap<String, String> params = buildRangeQueryParams(query, startTime, endTime, step);
        HttpTinyClient.HttpResult httpResult = null;
        PromResponse<MetricResult> response = null;
        try {
            httpResult = HttpTinyClient.httpGet(QUERY_RANGE, params);
            response = deserializeStrWithGeneric(MetricResult.class, httpResult.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public PromResponse<VectorResult> query(String query, Long time) {
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("time", AdminTimeUtil.millStamp2UTC(time));
        HttpTinyClient.HttpResult httpResult = null;
        PromResponse<VectorResult> response = null;
        try {
            httpResult = HttpTinyClient.httpGet(QUERY, params);
            response = deserializeStrWithGeneric(VectorResult.class, httpResult.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 查询服务流量/minute
     *
     * @param namespaceId
     * @param serviceName
     * @param startTime
     * @param endTime
     * @param step
     * @return
     */
    public MetricResult queryServiceRequestCountMetric(String namespaceId, String serviceName, Long startTime,
            Long endTime, Long step) {
        String querySql = "round(sum(increase(femas_http_client_requests_seconds_count{local_namespace=\"" + namespaceId
                + "\",remote_service=\"" + serviceName + "\"}[1m])),1)";
        HashMap<String, String> params = buildRangeQueryParams(querySql, startTime, endTime, step);
        HttpTinyClient.HttpResult httpResult = null;
        PromResponse<MetricResult> response = null;
        try {
            httpResult = HttpTinyClient.httpGet(QUERY_RANGE, params);
            response = deserializeStrWithGeneric(MetricResult.class, httpResult.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (response == null || CollectionUtil.isEmpty(response.getData().getResult())) {
            return new MetricResult(new HashMap<>(), new ArrayList<>());
        }
        return (MetricResult) response.getData().getResult().get(0);
    }

    public HashMap<String, String> buildRangeQueryParams(String query, Long startTime, Long endTime, Long step) {
        HashMap<String, String> params = new HashMap<>();
        params.put("query", query);
        params.put("start", AdminTimeUtil.millStamp2UTC(startTime));
        params.put("end", AdminTimeUtil.millStamp2UTC(endTime));
        params.put("step", step + "s");
        return params;
    }

}
