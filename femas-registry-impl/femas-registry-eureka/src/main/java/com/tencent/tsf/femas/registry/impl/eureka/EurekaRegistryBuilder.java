package com.tencent.tsf.femas.registry.impl.eureka;

import com.netflix.discovery.shared.resolver.DefaultEndpoint;
import com.netflix.discovery.shared.resolver.EurekaEndpoint;
import com.netflix.discovery.shared.transport.EurekaHttpClient;
import com.tencent.tsf.femas.common.AbstractRegistryBuilder;
import com.tencent.tsf.femas.common.exception.FemasRegisterDescribeException;
import com.tencent.tsf.femas.registry.impl.eureka.naming.EurekaNamingService;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.http.RestTemplateTransportClientFactory;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:39
 * @Version 1.0
 */
public class EurekaRegistryBuilder extends AbstractRegistryBuilder<EurekaNamingService> {

    private static final  Logger log = LoggerFactory.getLogger(EurekaRegistryBuilder.class);

    private static final  String EUREKA_SUFFIX = "/eureka";

    private static final  String EUREKA_PREFIX = "http://";

    @Override
    public EurekaNamingService build(Supplier<String> serverAddressSupplier, String namespace)
            throws FemasRegisterDescribeException {
        try {
            RestTemplateTransportClientFactory restTemplateTransportClientFactory =
                    new RestTemplateTransportClientFactory();
            EurekaEndpoint eurekaEndpoint = new DefaultEndpoint(
                    EUREKA_PREFIX.concat(serverAddressSupplier.get().concat(EUREKA_SUFFIX)));
            EurekaHttpClient eurekaHttpClient = restTemplateTransportClientFactory.newClient(eurekaEndpoint);
            return new EurekaNamingService(eurekaHttpClient);
        } catch (Exception e) {
            log.error("eureka Registry Build failed ", e);
            throw new FemasRegisterDescribeException(e);
        }
    }

}
