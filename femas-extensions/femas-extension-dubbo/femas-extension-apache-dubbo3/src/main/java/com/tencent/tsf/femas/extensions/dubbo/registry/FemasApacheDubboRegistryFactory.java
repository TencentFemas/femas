package com.tencent.tsf.femas.extensions.dubbo.registry;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

@Activate
public class FemasApacheDubboRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected Registry createRegistry(URL url) {
        return new FemasApacheDubboRegistry(url);
    }
}
