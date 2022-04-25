package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.kubernetes.KubernetesClientProperties;
import com.tencent.tsf.femas.common.kubernetes.KubernetesClientServicesFunction;
import com.tencent.tsf.femas.common.kubernetes.KubernetesDiscoveryProperties;
import com.tencent.tsf.femas.constant.IgnorePrefix;
import com.tencent.tsf.femas.util.EnvUtil;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;


@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    private final static Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    @Value("${femas.ignore.prefix:true}")
    private boolean ignoreSDKPrefix;

    @Bean
    public AdminInterceptor adminInterceptor() {
        return new AdminInterceptor();
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> myWebServerFactoryCustomizer() {
        return new WebServerFactoryCustomizer<ConfigurableWebServerFactory>() {
            @Override
            public void customize(ConfigurableWebServerFactory factory) {
                String FEMAS_BASE_PATH = EnvUtil.getFemasPrefix();
                ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, FEMAS_BASE_PATH + "/index");
                factory.addErrorPages(error404Page);
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String femasPrefix = EnvUtil.getFemasPrefix();
        String sdkPath = "/atom/v1/sdk/**";
        if (!ignoreSDKPrefix) {
            sdkPath = femasPrefix + sdkPath;
        }
        registry.addInterceptor(adminInterceptor()).addPathPatterns(femasPrefix + "/atom/**")
                .excludePathPatterns(femasPrefix + "/atom/v1/auth/login",//登录
                        femasPrefix + "/atom/v1/listener/**",
                        sdkPath);//sdk侧请求接口
    }

    @Override
    // 添加controller公共前缀
    public void configurePathMatch(PathMatchConfigurer configurer) {
        String FEMAS_BASE_PATH = EnvUtil.getFemasPrefix();
        configurer.addPathPrefix(FEMAS_BASE_PATH, c -> {
            // 剔除sdk访问接口前缀
            if (c.isAnnotationPresent(IgnorePrefix.class)) {
                return !ignoreSDKPrefix;
            }
            if (c.isAnnotationPresent(RestController.class) || c.isAnnotationPresent(Controller.class)) {
                return true;
            }
            return false;
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String FEMAS_BASE_PATH = EnvUtil.getFemasPrefix();
        registry.addResourceHandler(FEMAS_BASE_PATH + "/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler(FEMAS_BASE_PATH + "/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler(FEMAS_BASE_PATH + "/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler(FEMAS_BASE_PATH + "/cdn/**").addResourceLocations("classpath:/static/cdn/");
        registry.addResourceHandler(FEMAS_BASE_PATH + "/icon/**").addResourceLocations("classpath:/static/icon/");
        registry.addResourceHandler("/index.html").addResourceLocations("/index");
    }

    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config kubernetesClientConfig(KubernetesClientProperties kubernetesClientProperties) {
        Config base = Config.autoConfigure(null);
        Config properties = new ConfigBuilder(base)
                // Only set values that have been explicitly specified
                .withMasterUrl(or(kubernetesClientProperties.getMasterUrl(), base.getMasterUrl()))
                .withApiVersion(or(kubernetesClientProperties.getApiVersion(), base.getApiVersion()))
                .withNamespace(or(kubernetesClientProperties.getNamespace(), base.getNamespace()))
                .withUsername(or(kubernetesClientProperties.getUsername(), base.getUsername()))
                .withPassword(or(kubernetesClientProperties.getPassword(), base.getPassword()))

                .withOauthToken(or(kubernetesClientProperties.getOauthToken(), base.getOauthToken()))
                .withCaCertFile(or(kubernetesClientProperties.getCaCertFile(), base.getCaCertFile()))
                .withCaCertData(or(kubernetesClientProperties.getCaCertData(), base.getCaCertData()))

                .withClientKeyFile(or(kubernetesClientProperties.getClientKeyFile(), base.getClientKeyFile()))
                .withClientKeyData(or(kubernetesClientProperties.getClientKeyData(), base.getClientKeyData()))

                .withClientCertFile(or(kubernetesClientProperties.getClientCertFile(), base.getClientCertFile()))
                .withClientCertData(or(kubernetesClientProperties.getClientCertData(), base.getClientCertData()))

                // No magic is done for the properties below so we leave them as is.
                .withClientKeyAlgo(or(kubernetesClientProperties.getClientKeyAlgo(), base.getClientKeyAlgo()))
                .withClientKeyPassphrase(
                        or(kubernetesClientProperties.getClientKeyPassphrase(), base.getClientKeyPassphrase()))
                .withConnectionTimeout(
                        orDurationInt(kubernetesClientProperties.getConnectionTimeout(), base.getConnectionTimeout()))
                .withRequestTimeout(
                        orDurationInt(kubernetesClientProperties.getRequestTimeout(), base.getRequestTimeout()))
                .withRollingTimeout(
                        orDurationLong(kubernetesClientProperties.getRollingTimeout(), base.getRollingTimeout()))
                .withTrustCerts(or(kubernetesClientProperties.isTrustCerts(), base.isTrustCerts()))
                .withHttpProxy(or(kubernetesClientProperties.getHttpProxy(), base.getHttpProxy()))
                .withHttpsProxy(or(kubernetesClientProperties.getHttpsProxy(), base.getHttpsProxy()))
                .withProxyUsername(or(kubernetesClientProperties.getProxyUsername(), base.getProxyUsername()))
                .withProxyPassword(or(kubernetesClientProperties.getProxyPassword(), base.getProxyPassword()))
                .withNoProxy(or(kubernetesClientProperties.getNoProxy(), base.getNoProxy())).build();

        if (properties.getNamespace() == null || properties.getNamespace().isEmpty()) {
            log.warn("No namespace has been detected. Please specify "
                    + "KUBERNETES_NAMESPACE env var, or use a later kubernetes version (1.3 or later)");
        }
        return properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public KubernetesClient kubernetesClient(Config config) {
        return new DefaultKubernetesClient(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public KubernetesClientProperties kubernetesClientProperties() {
        return new KubernetesClientProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public KubernetesDiscoveryProperties kubernetesDiscoveryProperties() {
        return new KubernetesDiscoveryProperties();
    }

    @Bean
    public KubernetesClientServicesFunction servicesFunction(KubernetesDiscoveryProperties properties) {
        if (properties.getServiceLabels().isEmpty()) {
            if (properties.isAllNamespaces()) {
                return (client) -> client.services().inAnyNamespace();
            }
            else {
                return KubernetesClient::services;
            }
        }
        else {
            if (properties.isAllNamespaces()) {
                return (client) -> client.services().inAnyNamespace().withLabels(properties.getServiceLabels());
            }
            else {
                return (client) -> client.services().withLabels(properties.getServiceLabels());
            }
        }
    }

    private static <D> D or(D left, D right) {
        return left != null ? left : right;
    }

    private static Integer orDurationInt(Duration left, Integer right) {
        return left != null ? (int) left.toMillis() : right;
    }

    private static Long orDurationLong(Duration left, Long right) {
        return left != null ? left.toMillis() : right;
    }

}