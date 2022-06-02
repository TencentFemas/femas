package com.tencent.tsf.femas.config.grpc.paas;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 *添加一个服务
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.46.0)",
    comments = "Source: PaasPolling.proto")
public final class PaasPollingGrpc {

  private PaasPollingGrpc() {}

  public static final String SERVICE_NAME = "grpc.PaasPolling";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.SimpleParam,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getFetchBreakerRuleMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "fetchBreakerRule",
      requestType = com.tencent.tsf.femas.config.grpc.paas.SimpleParam.class,
      responseType = com.tencent.tsf.femas.config.grpc.paas.PollingResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.SimpleParam,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getFetchBreakerRuleMethod() {
    io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.SimpleParam, com.tencent.tsf.femas.config.grpc.paas.PollingResult> getFetchBreakerRuleMethod;
    if ((getFetchBreakerRuleMethod = PaasPollingGrpc.getFetchBreakerRuleMethod) == null) {
      synchronized (PaasPollingGrpc.class) {
        if ((getFetchBreakerRuleMethod = PaasPollingGrpc.getFetchBreakerRuleMethod) == null) {
          PaasPollingGrpc.getFetchBreakerRuleMethod = getFetchBreakerRuleMethod =
              io.grpc.MethodDescriptor.<com.tencent.tsf.femas.config.grpc.paas.SimpleParam, com.tencent.tsf.femas.config.grpc.paas.PollingResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "fetchBreakerRule"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.SimpleParam.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.PollingResult.getDefaultInstance()))
              .setSchemaDescriptor(new PaasPollingMethodDescriptorSupplier("fetchBreakerRule"))
              .build();
        }
      }
    }
    return getFetchBreakerRuleMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceApiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "reportServiceApi",
      requestType = com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest.class,
      responseType = com.tencent.tsf.femas.config.grpc.paas.PollingResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceApiMethod() {
    io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceApiMethod;
    if ((getReportServiceApiMethod = PaasPollingGrpc.getReportServiceApiMethod) == null) {
      synchronized (PaasPollingGrpc.class) {
        if ((getReportServiceApiMethod = PaasPollingGrpc.getReportServiceApiMethod) == null) {
          PaasPollingGrpc.getReportServiceApiMethod = getReportServiceApiMethod =
              io.grpc.MethodDescriptor.<com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "reportServiceApi"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.PollingResult.getDefaultInstance()))
              .setSchemaDescriptor(new PaasPollingMethodDescriptorSupplier("reportServiceApi"))
              .build();
        }
      }
    }
    return getReportServiceApiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "reportServiceEvent",
      requestType = com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest.class,
      responseType = com.tencent.tsf.femas.config.grpc.paas.PollingResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceEventMethod() {
    io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult> getReportServiceEventMethod;
    if ((getReportServiceEventMethod = PaasPollingGrpc.getReportServiceEventMethod) == null) {
      synchronized (PaasPollingGrpc.class) {
        if ((getReportServiceEventMethod = PaasPollingGrpc.getReportServiceEventMethod) == null) {
          PaasPollingGrpc.getReportServiceEventMethod = getReportServiceEventMethod =
              io.grpc.MethodDescriptor.<com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "reportServiceEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.PollingResult.getDefaultInstance()))
              .setSchemaDescriptor(new PaasPollingMethodDescriptorSupplier("reportServiceEvent"))
              .build();
        }
      }
    }
    return getReportServiceEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getInitNamespaceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "initNamespace",
      requestType = com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest.class,
      responseType = com.tencent.tsf.femas.config.grpc.paas.PollingResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest,
      com.tencent.tsf.femas.config.grpc.paas.PollingResult> getInitNamespaceMethod() {
    io.grpc.MethodDescriptor<com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult> getInitNamespaceMethod;
    if ((getInitNamespaceMethod = PaasPollingGrpc.getInitNamespaceMethod) == null) {
      synchronized (PaasPollingGrpc.class) {
        if ((getInitNamespaceMethod = PaasPollingGrpc.getInitNamespaceMethod) == null) {
          PaasPollingGrpc.getInitNamespaceMethod = getInitNamespaceMethod =
              io.grpc.MethodDescriptor.<com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest, com.tencent.tsf.femas.config.grpc.paas.PollingResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "initNamespace"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.tencent.tsf.femas.config.grpc.paas.PollingResult.getDefaultInstance()))
              .setSchemaDescriptor(new PaasPollingMethodDescriptorSupplier("initNamespace"))
              .build();
        }
      }
    }
    return getInitNamespaceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PaasPollingStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaasPollingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaasPollingStub>() {
        @java.lang.Override
        public PaasPollingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaasPollingStub(channel, callOptions);
        }
      };
    return PaasPollingStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PaasPollingBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaasPollingBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaasPollingBlockingStub>() {
        @java.lang.Override
        public PaasPollingBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaasPollingBlockingStub(channel, callOptions);
        }
      };
    return PaasPollingBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PaasPollingFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PaasPollingFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PaasPollingFutureStub>() {
        @java.lang.Override
        public PaasPollingFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PaasPollingFutureStub(channel, callOptions);
        }
      };
    return PaasPollingFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   *添加一个服务
   * </pre>
   */
  public static abstract class PaasPollingImplBase implements io.grpc.BindableService {

    /**
     */
    public void fetchBreakerRule(com.tencent.tsf.femas.config.grpc.paas.SimpleParam request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFetchBreakerRuleMethod(), responseObserver);
    }

    /**
     */
    public void reportServiceApi(com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportServiceApiMethod(), responseObserver);
    }

    /**
     */
    public void reportServiceEvent(com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportServiceEventMethod(), responseObserver);
    }

    /**
     */
    public void initNamespace(com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInitNamespaceMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getFetchBreakerRuleMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.tencent.tsf.femas.config.grpc.paas.SimpleParam,
                com.tencent.tsf.femas.config.grpc.paas.PollingResult>(
                  this, METHODID_FETCH_BREAKER_RULE)))
          .addMethod(
            getReportServiceApiMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest,
                com.tencent.tsf.femas.config.grpc.paas.PollingResult>(
                  this, METHODID_REPORT_SERVICE_API)))
          .addMethod(
            getReportServiceEventMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest,
                com.tencent.tsf.femas.config.grpc.paas.PollingResult>(
                  this, METHODID_REPORT_SERVICE_EVENT)))
          .addMethod(
            getInitNamespaceMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest,
                com.tencent.tsf.femas.config.grpc.paas.PollingResult>(
                  this, METHODID_INIT_NAMESPACE)))
          .build();
    }
  }

  /**
   * <pre>
   *添加一个服务
   * </pre>
   */
  public static final class PaasPollingStub extends io.grpc.stub.AbstractAsyncStub<PaasPollingStub> {
    private PaasPollingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaasPollingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaasPollingStub(channel, callOptions);
    }

    /**
     */
    public void fetchBreakerRule(com.tencent.tsf.femas.config.grpc.paas.SimpleParam request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFetchBreakerRuleMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void reportServiceApi(com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportServiceApiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void reportServiceEvent(com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportServiceEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void initNamespace(com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest request,
        io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInitNamespaceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   *添加一个服务
   * </pre>
   */
  public static final class PaasPollingBlockingStub extends io.grpc.stub.AbstractBlockingStub<PaasPollingBlockingStub> {
    private PaasPollingBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaasPollingBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaasPollingBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.tencent.tsf.femas.config.grpc.paas.PollingResult fetchBreakerRule(com.tencent.tsf.femas.config.grpc.paas.SimpleParam request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFetchBreakerRuleMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.tencent.tsf.femas.config.grpc.paas.PollingResult reportServiceApi(com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportServiceApiMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.tencent.tsf.femas.config.grpc.paas.PollingResult reportServiceEvent(com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportServiceEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.tencent.tsf.femas.config.grpc.paas.PollingResult initNamespace(com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInitNamespaceMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   *添加一个服务
   * </pre>
   */
  public static final class PaasPollingFutureStub extends io.grpc.stub.AbstractFutureStub<PaasPollingFutureStub> {
    private PaasPollingFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PaasPollingFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PaasPollingFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.tencent.tsf.femas.config.grpc.paas.PollingResult> fetchBreakerRule(
        com.tencent.tsf.femas.config.grpc.paas.SimpleParam request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFetchBreakerRuleMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.tencent.tsf.femas.config.grpc.paas.PollingResult> reportServiceApi(
        com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportServiceApiMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.tencent.tsf.femas.config.grpc.paas.PollingResult> reportServiceEvent(
        com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportServiceEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.tencent.tsf.femas.config.grpc.paas.PollingResult> initNamespace(
        com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInitNamespaceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FETCH_BREAKER_RULE = 0;
  private static final int METHODID_REPORT_SERVICE_API = 1;
  private static final int METHODID_REPORT_SERVICE_EVENT = 2;
  private static final int METHODID_INIT_NAMESPACE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PaasPollingImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PaasPollingImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_FETCH_BREAKER_RULE:
          serviceImpl.fetchBreakerRule((com.tencent.tsf.femas.config.grpc.paas.SimpleParam) request,
              (io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult>) responseObserver);
          break;
        case METHODID_REPORT_SERVICE_API:
          serviceImpl.reportServiceApi((com.tencent.tsf.femas.config.grpc.paas.ServiceApiRequest) request,
              (io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult>) responseObserver);
          break;
        case METHODID_REPORT_SERVICE_EVENT:
          serviceImpl.reportServiceEvent((com.tencent.tsf.femas.config.grpc.paas.ReportEventRequest) request,
              (io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult>) responseObserver);
          break;
        case METHODID_INIT_NAMESPACE:
          serviceImpl.initNamespace((com.tencent.tsf.femas.config.grpc.paas.InitNamespaceRequest) request,
              (io.grpc.stub.StreamObserver<com.tencent.tsf.femas.config.grpc.paas.PollingResult>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class PaasPollingBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PaasPollingBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.tencent.tsf.femas.config.grpc.paas.LongPollingServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PaasPolling");
    }
  }

  private static final class PaasPollingFileDescriptorSupplier
      extends PaasPollingBaseDescriptorSupplier {
    PaasPollingFileDescriptorSupplier() {}
  }

  private static final class PaasPollingMethodDescriptorSupplier
      extends PaasPollingBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PaasPollingMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PaasPollingGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PaasPollingFileDescriptorSupplier())
              .addMethod(getFetchBreakerRuleMethod())
              .addMethod(getReportServiceApiMethod())
              .addMethod(getReportServiceEventMethod())
              .addMethod(getInitNamespaceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
