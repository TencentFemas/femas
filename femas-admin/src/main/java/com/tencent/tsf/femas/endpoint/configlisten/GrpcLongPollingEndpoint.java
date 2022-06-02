package com.tencent.tsf.femas.endpoint.configlisten;

import com.tencent.tsf.femas.config.grpc.paas.*;
import com.tencent.tsf.femas.context.ApplicationContextHelper;
import com.tencent.tsf.femas.service.namespace.NamespaceMangerService;
import com.tencent.tsf.femas.service.registry.ServiceManagerService;
import com.tencent.tsf.femas.service.rule.ConvertService;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;


/**
 * @ClassName Grpc
 * @Description TODO
 * @Author cstongwei
 * @Date 2022/5/30 10:28
 * @Version 1.0.0
 **/
@Slf4j
public class GrpcLongPollingEndpoint  extends PaasPollingGrpc.PaasPollingImplBase{

    ConvertService convertService;

    ServiceManagerService serviceManagerService;

    NamespaceMangerService namespaceMangerService;

    public GrpcLongPollingEndpoint() {
        convertService = ApplicationContextHelper.getBean(ConvertService.class);
        serviceManagerService = ApplicationContextHelper.getBean(ServiceManagerService.class);
        namespaceMangerService = ApplicationContextHelper.getBean(NamespaceMangerService.class);
    }

    @Override
    public void fetchBreakerRule(SimpleParam request, StreamObserver<PollingResult> responseObserver) {
        try {
            String fetchBreakerRule = convertService.convert(request.getParam());
            if(Context.current().isCancelled()){
                log.debug("request is cancelled!");
                responseObserver.onError(Status.CANCELLED.withDescription("request is cancelled!").asRuntimeException());
                return;
            }
            PollingResult result;
            if(fetchBreakerRule!=null){
                result = GrpcHepler.ok(fetchBreakerRule);
            }else{
                result = GrpcHepler.ok();
            }
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        }catch (Throwable e){
            responseObserver.onNext(GrpcHepler.fail(e.toString()));
            responseObserver.onCompleted();
            log.error("error",e);
        }

    }

    @Override
    public void reportServiceApi(ServiceApiRequest request, StreamObserver<PollingResult> responseObserver) {
        if (log.isDebugEnabled()) {
            log.debug("ServiceApiRequest: {}", request);
        }
        try {
            serviceManagerService.reportServiceApi(request.getNamespaceId(),
                    request.getServiceName(), request.getApplicationVersion(),
                    request.getData());
            if(Context.current().isCancelled()){
                log.debug("request is cancelled!");
                responseObserver.onError(Status.CANCELLED.withDescription("request is cancelled!").asRuntimeException());
                return;
            }
            responseObserver.onNext(GrpcHepler.ok());
            responseObserver.onCompleted();
        }catch (Throwable e){
            responseObserver.onNext(GrpcHepler.fail(e.getCause().toString()));
            responseObserver.onCompleted();
            log.error("error",e);
        }

    }

    @Override
    public void reportServiceEvent(ReportEventRequest request, StreamObserver<PollingResult> responseObserver) {
        try {
            serviceManagerService.reportServiceEvent(request.getNamespaceId(), request.getServiceName(), request.getEventId(), request.getData());
            if(Context.current().isCancelled()){
                log.debug("request is cancelled!");
                responseObserver.onError(Status.CANCELLED.withDescription("request is cancelled!").asRuntimeException());
                return;
            }
            responseObserver.onNext(GrpcHepler.ok());
            responseObserver.onCompleted();
        }catch (Throwable e){
            responseObserver.onNext(GrpcHepler.fail(e.getCause().toString()));
            responseObserver.onCompleted();
            log.error("error",e);
        }
    }

    @Override
    public void initNamespace(InitNamespaceRequest request, StreamObserver<PollingResult> responseObserver) {
        try {
            namespaceMangerService.initNamespace(request.getRegistryAddress(), request.getNamespaceId());
            if(Context.current().isCancelled()){
                log.debug("request is cancelled!");
                responseObserver.onError(Status.CANCELLED.withDescription("request is cancelled!").asRuntimeException());
                return;
            }
            responseObserver.onNext(GrpcHepler.ok());
            responseObserver.onCompleted();
        }catch (Throwable e){
            responseObserver.onNext(GrpcHepler.fail(e.getCause().toString()));
            responseObserver.onCompleted();
            log.error("error",e);
        }
    }
}
