package com.tencent.tsf.femas.registry.impl.k8s;

import com.tencent.tsf.femas.common.AbstractRegistryBuilder;


import com.tencent.tsf.femas.common.kubernetes.KubernetesClientProperties;
import com.tencent.tsf.femas.common.kubernetes.KubernetesDiscoveryProperties;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Supplier;


/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:39
 * @Version 1.0
 */
public class K8sRegistryBuilder extends AbstractRegistryBuilder<KubernetesClient> {

    private final static Logger log = LoggerFactory.getLogger(K8sRegistryBuilder.class);

    private static KubernetesClientProperties kubernetesClientProperties;
    private static KubernetesDiscoveryProperties kubernetesDiscoveryProperties;
    private static KubernetesClient kubernetesClient;
    private static Config config;


    public static KubernetesClientProperties getKubernetesClientProperties() {
        return kubernetesClientProperties;
    }

    public static KubernetesDiscoveryProperties getKubernetesDiscoveryProperties() {
        return kubernetesDiscoveryProperties;
    }

    @Override
    public KubernetesClient build(Supplier serverAddressSupplier, String namespace) {
        return kubernetesClient;
    }

    static {
        kubernetesClientProperties = new KubernetesClientProperties();
        kubernetesDiscoveryProperties=new KubernetesDiscoveryProperties();
        config = kubernetesClientConfig(kubernetesClientProperties);
        kubernetesClient = new DefaultKubernetesClient(config);
    }

    public static Config kubernetesClientConfig(KubernetesClientProperties kubernetesClientProperties) {
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
