package com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tencent.tsf.femas.adaptor.paas.common.FemasConstant;
import com.tencent.tsf.femas.adaptor.paas.governance.ratelimiter.entity.ReportResponse;
import com.tencent.tsf.femas.common.context.FemasContext;
import com.tencent.tsf.femas.common.entity.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestCollector {

    // RequestCollector 需要计数规则：
    // - 如果请求被放行，那么相应的服务 passCount 和全局 passCount 都要增加
    // - 如果请求被某一限定主调的规则拦住，那么相应的服务 blockCount 增加，全局 blockCount 不增加
    // - 如果请求被全局规则拦住，那么相应的全局 blockCount 增加，服务 blockCount 不增加
    private static final Logger LOG = LoggerFactory.getLogger(RequestCollector.class);
    RateLimitClientCache ratelimitClientCache;
    private Map<String, Integer> passCountByServiceName = new HashMap<>();
    private Map<String, Integer> blockCountByServiceName = new HashMap<>();
    private int passCountGlobally = 0;
    private int blockCountByGlobalLimit = 0;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private String insId = FemasContext.getSystemTag(FemasConstant.FEMAS_INSTANCE_ID);
    private Service service;
    private CloseableHttpClient httpClient;

    public RequestCollector(Service service, RateLimitClientCache ratelimitClientCache) {
        this.ratelimitClientCache = ratelimitClientCache;
        this.service = service;

        RequestConfig config = RequestConfig.custom().setConnectTimeout(500).setConnectionRequestTimeout(2000)
                .setSocketTimeout(2000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        scheduledExecutorService.scheduleWithFixedDelay(this::report, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static ReportResponse deserializeTagList(String buffer) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.fromJson(buffer, ReportResponse.class);
    }

    public synchronized void incrPassCount(String ruleId) {
        Integer previousCount = passCountByServiceName.putIfAbsent(ruleId, 1);
        if (previousCount != null) {
            passCountByServiceName.put(ruleId, previousCount + 1);
        }
        passCountGlobally++;
    }

    public synchronized void incrBlockCount(String ruleId) {
        Integer previousCount = blockCountByServiceName.putIfAbsent(ruleId, 1);
        if (previousCount != null) {
            blockCountByServiceName.put(ruleId, previousCount + 1);
        }
    }

    public synchronized void incrBlockCountByGlobalLimit() {
        blockCountByGlobalLimit++;
    }

    public synchronized void resetCount() {
        for (Map.Entry<String, Integer> entry : passCountByServiceName.entrySet()) {
            entry.setValue(0);
        }
        for (Map.Entry<String, Integer> entry : blockCountByServiceName.entrySet()) {
            entry.setValue(0);
        }
        passCountGlobally = 0;
        blockCountByGlobalLimit = 0;
    }

    public void setInsId(String insId) {
        this.insId = insId;
    }

    /**
     * 上报到中控
     */
    public void report() {
    }
}
