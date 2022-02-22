
package com.tencent.tsf.femas.registry.impl.k8s.serviceregistry;

import com.tencent.tsf.femas.common.RegistryEnum;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistry;
import com.tencent.tsf.femas.common.serviceregistry.ServiceRegistryFactory;

import java.util.Map;

/**
 * @author leo
 */
public class K8sServiceRegistryFactory implements ServiceRegistryFactory {
    @Override
    public ServiceRegistry getServiceRegistry(Map<String, String> configMap) {
        return new K8sServiceRegistry(configMap);
    }

    @Override
    public String getType() {
        return RegistryEnum.KUBERNETES.name();
    }
}
