package com.tencent.tsf.femas.adaptor.paas.config;

import com.google.gson.Gson;
import com.tencent.tsf.femas.common.context.Context;
import com.tencent.tsf.femas.common.context.factory.ContextFactory;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.httpclient.ApacheHttpClientHolder;
import com.tencent.tsf.femas.common.httpclient.client.AbstractHttpClient;
import com.tencent.tsf.femas.common.httpclient.factory.ApacheDefaultHttpClientFactory;
import com.tencent.tsf.femas.common.httpclient.factory.HttpClientFactory;
import com.tencent.tsf.femas.common.util.HttpHeaderKeys;
import com.tencent.tsf.femas.common.util.HttpResult;
import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.common.util.id.UIdGenerator;
import com.tencent.tsf.femas.config.AbstractConfigHttpClientManager;
import com.tencent.tsf.femas.config.FemasConfig;
import com.tencent.tsf.femas.governance.api.entity.ServiceApiRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FemasConfigHttpClientManager extends AbstractConfigHttpClientManager {

    private static final  Logger log = LoggerFactory.getLogger(FemasConfigHttpClientManager.class);
    private static final  String webContext = "/atom";
    private static final  String fetchKeyUrl = "/v1/sdk/fetchData";
    private static final  String reportCircuitEvent = "/v1/sdk/reportServiceEvent";
    private static final  String reportApis = "/v1/sdk/reportServiceApi";
    private static final  String intiNamespace = "/v1/sdk/initNamespace";
    private static final  int DEFAULT_READ_TIME_OUT_MILLIS = Integer
            .getInteger("femas.paas.config.client.readTimeOut", 50000);
    private static final  int DEFAULT_CON_TIME_OUT_MILLIS = Integer
            .getInteger("femas.paas.config.client.conTimeOut", 3000);
    private static Context commonContext = ContextFactory.getContextInstance();
    private static volatile FemasConfigHttpClientManager singleton = null;
    private volatile Context context = ContextFactory.getContextInstance();
    /**
     * 链接地址，上层manager封装
     */
    private String paasServerDomain;
    private String keyListenerUrl;
    private String reportCircuitEventUrl;
    private String reportApisUrl;
    private String initNamespaceUrl;
    private AbstractHttpClient httpClient;

    public FemasConfigHttpClientManager() {
        Map<String, String> configMap = commonContext.getRegistryConfigMap();
        HttpClientFactory httpClientFactory = new ApacheDefaultHttpClientFactory(DEFAULT_READ_TIME_OUT_MILLIS,
                DEFAULT_CON_TIME_OUT_MILLIS);

        String paasServerDomainByConfig = null;
        if (configMap != null) {
            paasServerDomainByConfig = configMap.get("paas_server_address");
        }
        if (StringUtils.isBlank(paasServerDomainByConfig)) {
            paasServerDomainByConfig = FemasConfig.getProperty("paas_server_address");
        }
        this.paasServerDomain = paasServerDomainByConfig;
        this.keyListenerUrl = paasServerDomain.concat(webContext).concat(fetchKeyUrl);
        this.reportCircuitEventUrl = paasServerDomain.concat(webContext).concat(reportCircuitEvent);
        this.reportApisUrl = paasServerDomain.concat(webContext).concat(reportApis);
        this.initNamespaceUrl = paasServerDomain.concat(webContext).concat(intiNamespace);
        this.httpClient = ApacheHttpClientHolder.getHttpClient(httpClientFactory);
    }

    public void reportEvent(Service service, String eventId, String data) {
        if (context.isEmptyPaasServer()) {
            log.debug("reportEvent failed, could not find the paas address profile");
            return;
        }
        final Map<String, Object> params = new HashMap<String, Object>(3);
        params.put("namespaceId", service.getNamespace());
        params.put("serviceName", service.getName());
        params.put("eventId", eventId);
        params.put("data", data);
        try {
            httpClient.post(reportCircuitEventUrl, builderHeader(), params, null);
        } catch (Exception e) {
            log.error("init namespace failed", e);
        }
    }

    public void reportApis(String namespaceId, String serviceName, String applicationVersion, String data) {
        if (context.isEmptyPaasServer()) {
            log.debug("reportApis failed ,could not find the paas address profile");
            return;
        }
        ServiceApiRequest serviceApiRequest = new ServiceApiRequest();
        serviceApiRequest.setNamespaceId(namespaceId);
        serviceApiRequest.setServiceName(serviceName);
        serviceApiRequest.setApplicationVersion(applicationVersion);
        serviceApiRequest.setData(data);
        try {
            // 后续使用 femas httpclient
            HttpEntity reqBody = new StringEntity(new Gson().toJson(serviceApiRequest), ContentType.APPLICATION_JSON);
            CloseableHttpClient closeableHttpClient = HttpClients.custom().build();
            HttpPost post = new HttpPost(reportApisUrl);
            post.setEntity(reqBody);
            CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(post);
            String result = EntityUtils.toString(closeableHttpResponse.getEntity());
            log.debug("result:{}", result);
        } catch (Exception e) {
            // 无配置 paas server 时，报错不打印
            log.warn("config http manager reportApis failed, msg:{}", e.getMessage());
        }
    }

    public String fetchKVValue(String key, String namespaceId) {

        final Map<String, Object> params = new HashMap<>(3);
        params.put("namespaceId", namespaceId);
        params.put("key", key);
        if (context.isEmptyPaasServer()) {
            log.debug("fetchKVValue failed , could not find the paas address profile");
            return null;
        }
        HttpResult<String> httpResult = null;
        try {
            httpResult = httpClient.get(keyListenerUrl, builderHeader(), params);
        } catch (Exception e) {
            log.error("config http manager fetchKVValue failed", e);
        }
        if (httpResult != null) {
            return httpResult.getData();
        }
        return null;
    }

    private Properties loadProperties(String propertyFileName) {
        InputStreamReader in = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            in = new InputStreamReader(loader.getResourceAsStream(propertyFileName), "UTF-8");
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                return prop;
            }
        } catch (IOException e) {
            log.error("load {} error!", propertyFileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("close {} error!", propertyFileName);
                }
            }
        }
        return null;
    }

    public Map<String, String> builderHeader() {
        Map<String, String> header = new HashMap<>();
        //标识sdk client版本号，从默认配置文件中获取
        header.put(HttpHeaderKeys.USER_AGENT_HEADER, FemasConfig.getProperty("femas.sdk.client.version"));
        header.put(HttpHeaderKeys.ACCEPT_ENCODING, "gzip,deflate,sdch");
        header.put(HttpHeaderKeys.CONNECTION, "Keep-Alive");
        header.put(HttpHeaderKeys.REQUEST_ID, UIdGenerator.generateUid());
        header.put(HttpHeaderKeys.CONTENT_TYPE, "application/json;charset=UTF-8");
        return header;
    }

    public void initNamespace(String registryAddress, String namespaceId) {
        if (StringUtils.isEmpty(namespaceId)) {
            log.error("namespace is empty");
        }
        if (context.isEmptyPaasServer()) {
            log.debug("initNamespace failed , could not find the paas address profile");
            return;
        }
        final Map<String, Object> params = new HashMap<>(2);
        params.put("namespaceId", namespaceId);
        params.put("registryAddress", registryAddress);
        HttpResult<String> httpResult;
        try {
            httpResult = httpClient.post(initNamespaceUrl, builderHeader(), params, null);
            if (httpResult.getCode().startsWith("4") || httpResult.getCode().startsWith("5")) {
                log.error("init namespace failed {}", httpResult.getCode());
            }
        } catch (Exception e) {
            log.error("init namespace failed, msg:{}", e.getMessage());
        }
    }

}
