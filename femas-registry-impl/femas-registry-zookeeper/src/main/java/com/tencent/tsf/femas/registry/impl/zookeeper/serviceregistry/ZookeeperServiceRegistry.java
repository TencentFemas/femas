package com.tencent.tsf.femas.registry.impl.zookeeper.serviceregistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import org.apache.zookeeper.*;

import java.util.Map;

/**
 * @author huyuanxin
 */
public class ZookeeperServiceRegistry extends AbstractServiceRegistry {

    private ZooKeeper zooKeeper;
    private final ObjectMapper objectMapper;

    public ZookeeperServiceRegistry(Map<String, String> configMap) {
        try {
            String connectString = configMap.get(RegistryConstants.REGISTRY_HOST) + ":" + configMap.get(RegistryConstants.REGISTRY_PORT);
            this.zooKeeper = new ZooKeeper(connectString, 3000, watchedEvent -> {
                if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState() && Watcher.Event.EventType.None == watchedEvent.getType()) {
                    logger.info("zookeeper server connect success!");
                }
            });
            if (zooKeeper.exists("/femas", false) == null) {
                // init
                zooKeeper.create("/femas", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            logger.error("Error create zookeeper registry with:{0}", e);
        }
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        try {
            String namespace = serviceInstance.getService().getNamespace();
            buildNameSpace(namespace);
            String name = serviceInstance.getService().getName();
            buildServiceName(namespace, name);
            zooKeeper.create(
                    buildPath(namespace, name, serviceInstance.getId()),
                    objectMapper.writeValueAsBytes(serviceInstance),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL
            );
            logger.info("Service {} registered.", serviceInstance);
        } catch (Exception e) {
            logger.error("Error deregisterInstance service with zookeeper:{} ", serviceInstance, e);
        }
    }

    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        try {
            String namespace = serviceInstance.getService().getNamespace();
            String name = serviceInstance.getService().getName();
            zooKeeper.delete(buildPath(namespace, name, serviceInstance.getId()), -1);
            logger.info("Deregister service with zookeeper: {} success.", serviceInstance);
        } catch (Exception e) {
            logger.error("Error registering service with zookeeper: " + serviceInstance, e);
        }
    }

    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {
        // do nothing
    }

    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        return null;
    }

    private void buildPath(String path) throws InterruptedException, KeeperException {
        if (zooKeeper.exists(path, false) == null) {
            zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    private void buildNameSpace(String nameSpace) throws InterruptedException, KeeperException {
        String path = "/femas/" + nameSpace;
        buildPath(path);
    }

    private void buildServiceName(String nameSpace, String serviceName) throws InterruptedException, KeeperException {
        String path = "/femas/" + nameSpace + "/" + serviceName;
        buildPath(path);
    }

    private String buildPath(String nameSpace, String serviceName, String id) {
        return "/femas/" + nameSpace + "/" + serviceName + "/" + id;
    }

}
