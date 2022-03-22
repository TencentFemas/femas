package com.tencent.tsf.femas.registry.impl.polaris.serviceregistry;

import com.tencent.polaris.api.core.ProviderAPI;
import com.tencent.polaris.api.rpc.InstanceHeartbeatRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tencent.tsf.femas.common.RegistryConstants.DEFAULT_THREAD_POOL_SIZE;

/**
 * @author huyuanxin
 */
public class PolarisBeatReactor {

    Logger logger = LoggerFactory.getLogger(PolarisBeatReactor.class);

    private final AtomicBoolean heatBeatingInProgress = new AtomicBoolean(true);
    private final ScheduledExecutorService executorService;
    private final Map<String, InstanceInfo> polarisBeat = new ConcurrentHashMap<>();

    ProviderAPI providerApi;

    public PolarisBeatReactor(ProviderAPI providerApi) {
        this.providerApi = providerApi;
        executorService = new ScheduledThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("com.tencent.femas.polaris.beat.sender");
            return thread;
        });
    }

    public void addInstance(String key, InstanceInfo instanceInfo) {
        polarisBeat.put(key, instanceInfo);
        // polaris每个任务心跳都可以不一样,因此，马上发送一次心跳再进入队列里面进行定时任务
        sentHeartbeat(instanceInfo);
        if (this.heatBeatingInProgress.compareAndSet(true, false)) {
            fireHeatBeat();
        }
    }

    public void removeInstance(String key){
        polarisBeat.remove(key);
    }

    private void fireHeatBeat() {
        executorService.schedule(new BeatProcessor(), 0, TimeUnit.SECONDS);
    }

    class BeatProcessor implements Runnable {

        @Override
        public void run() {
            try {
                for (Map.Entry<String, InstanceInfo> entry : polarisBeat.entrySet()) {
                    InstanceInfo instanceInfo = entry.getValue();
                    executorService.schedule(() -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("[BEAT] adding beat: {} to beat map.", instanceInfo.getInstanceId());
                        }
                        sentHeartbeat(instanceInfo);
                    }, instanceInfo.getTtl(), TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                logger.error("[CLIENT-BEAT] Exception while scheduling beat.", e);
            }
        }
    }

    private void sentHeartbeat(InstanceInfo instanceInfo) {
        InstanceHeartbeatRequest instanceHeartbeatRequest = new InstanceHeartbeatRequest();
        instanceHeartbeatRequest.setInstanceID(instanceInfo.getInstanceId());
        instanceHeartbeatRequest.setNamespace(instanceInfo.getNamespace());
        instanceHeartbeatRequest.setService(instanceInfo.getService());
        instanceHeartbeatRequest.setPort(instanceInfo.getPort());
        instanceHeartbeatRequest.setHost(instanceInfo.getHost());
        providerApi.heartbeat(instanceHeartbeatRequest);
    }
}

class InstanceInfo {
    private String instanceId;
    private String namespace;
    private String service;
    private Integer port;
    private String host;
    private Integer ttl;

    public InstanceInfo(String instanceId, String namespace, String service, Integer port, String host, Integer ttl) {
        this.instanceId = instanceId;
        this.namespace = namespace;
        this.service = service;
        this.port = port;
        this.host = host;
        this.ttl = ttl;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

}
