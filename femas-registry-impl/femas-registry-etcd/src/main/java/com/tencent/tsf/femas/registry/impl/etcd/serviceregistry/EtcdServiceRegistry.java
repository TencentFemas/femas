package com.tencent.tsf.femas.registry.impl.etcd.serviceregistry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.tsf.femas.common.RegistryConstants;
import com.tencent.tsf.femas.common.entity.EndpointStatus;
import com.tencent.tsf.femas.common.entity.Service;
import com.tencent.tsf.femas.common.entity.ServiceInstance;
import com.tencent.tsf.femas.common.serviceregistry.AbstractServiceRegistry;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.common.exception.ErrorCode;
import io.etcd.jetcd.common.exception.EtcdException;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.StreamObserver;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author huyuanxin
 */
public class EtcdServiceRegistry extends AbstractServiceRegistry {

    private static final String KEY_STRING_FORMAT = "%s-%s-%s";

    private final KV kvClient;

    private final Lease leaseClient;

    private final StreamObserver<LeaseKeepAliveResponse> observer;

    private final ObjectMapper objectMapper;

    public EtcdServiceRegistry(Map<String, String> configMap) {
        Client client = Client.builder().endpoints(configMap.get(RegistryConstants.REGISTRY_HOST)).build();
        this.kvClient = client.getKVClient();
        this.leaseClient = client.getLeaseClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.observer = new StreamObserver<LeaseKeepAliveResponse>() {

            @Override
            public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                logger.info("Sending heartbeat");
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof EtcdException) {
                    EtcdException exception = (EtcdException) throwable;
                    if (exception.getErrorCode().equals(ErrorCode.NOT_FOUND)) {
                        // if you doDeregister service instance,it will error once
                        // and the message would be etcdserver: requested lease not found
                        logger.warn("lease id maybe remove by doRegister");
                        return;
                    }
                }
                logger.error("Error with heartbeat:", throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("Sending heartbeat completed");
            }
        };

    }

    /**
     * 实际注册实例至注册中心的办法
     *
     * @param serviceInstance serviceInstance
     */
    @Override
    protected void doRegister(ServiceInstance serviceInstance) {
        try {
            // registry and do heartbeat
            long leaseId = leaseClient.grant(serviceInstance.getTtl()).get().getID();
            leaseClient.keepAlive(leaseId, observer);
            kvClient.put(
                    formatByteSequence(serviceInstance),
                    ByteSequence.from(objectMapper.writeValueAsBytes(serviceInstance)),
                    PutOption.newBuilder().withLeaseId(leaseId).build()
            );
        } catch (Exception e) {
            logger.error("Error with registry:", e);
        }
    }

    /**
     * 实际将实例反注册至注册中心的办法
     *
     * @param serviceInstance serviceInstance
     */
    @Override
    protected void doDeregister(ServiceInstance serviceInstance) {
        try {
            CompletableFuture<DeleteResponse> deleteFuture = kvClient.delete(
                    formatByteSequence(serviceInstance),
                    DeleteOption.newBuilder().withPrevKV(true).build()
            );
            // stop heartbeats
            leaseClient.revoke(deleteFuture.get().getPrevKvs().get(0).getLease());
        } catch (Exception e) {
            logger.error("Error with deregister:", e);
        }
    }

    /**
     * Sets the status of the registration. The status values are determined
     * by the individual implementations.
     *
     * @param serviceInstance the serviceInstance to update
     * @param status          the status to set
     */
    @Override
    public void setStatus(ServiceInstance serviceInstance, EndpointStatus status) {
        try {
            serviceInstance.setStatus(status);
            kvClient.put(
                    formatByteSequence(serviceInstance),
                    ByteSequence.from(objectMapper.writeValueAsBytes(serviceInstance))
            );
        } catch (Exception e) {
            logger.error("Error with registry:", e);
        }
    }

    /**
     * Gets the status of a particular registration.
     *
     * @param serviceInstance the serviceInstance to query
     * @return the status of the serviceInstance
     */
    @Override
    public EndpointStatus getStatus(ServiceInstance serviceInstance) {
        try {
            CompletableFuture<GetResponse> response = kvClient.get(
                    formatByteSequence(serviceInstance),
                    GetOption.newBuilder().isPrefix(false).build()
            );
            String content = response.get().getKvs().get(0).getValue().toString();
            ServiceInstance find = objectMapper.readValue(content, ServiceInstance.class);
            return find.getStatus();
        } catch (Exception e) {
            logger.error("Error with get status:", e);
        }
        return EndpointStatus.UNKNOWN;
    }

    private ByteSequence formatByteSequence(ServiceInstance serviceInstance) {
        Service service = serviceInstance.getService();
        String keyString = String.format(
                KEY_STRING_FORMAT,
                service.getName(),
                service.getNamespace(),
                serviceInstance.getId()
        );
        return ByteSequence.from(keyString, StandardCharsets.UTF_8);
    }
}
