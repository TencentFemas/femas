package com.tencent.tsf.femas.registry.impl.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.tencent.tsf.femas.common.AbstractRegistryBuilder;
import com.tencent.tsf.femas.common.exception.FemasRegisterDescribeException;
import java.util.Properties;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/24 15:39
 * @Version 1.0
 */
public class NacosRegistryBuilder extends AbstractRegistryBuilder<NamingService> {

    private static final  Logger log = LoggerFactory.getLogger(NacosRegistryBuilder.class);

    @Override
    public NamingService build(Supplier serverAddressSupplier, String namespace) throws FemasRegisterDescribeException {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, (String) serverAddressSupplier.get());
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        properties
                .setProperty("namingClientBeatThreadCount", String.valueOf(Runtime.getRuntime().availableProcessors()));
        try {
            return NamingFactory.createNamingService(properties);
        } catch (Exception e) {
            log.error("Nacos Registry Build failed ", e);
            throw new FemasRegisterDescribeException(e);
        }
    }

}
