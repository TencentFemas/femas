package com.tencent.tsf.femas;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.tencent.tsf.femas.config.grpc.paas.GrpcHepler;
import com.tencent.tsf.femas.config.thrift.paas.PaasPolling;
import com.tencent.tsf.femas.config.thrift.paas.ThriftHelper;
import com.tencent.tsf.femas.endpoint.configlisten.GrpcLongPollingEndpoint;
import com.tencent.tsf.femas.endpoint.configlisten.ThriftLongPollingEndpoint;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.layered.TFramedTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/6 16:01
 * @Version 1.0
 */
@EnableScheduling
@SpringBootApplication
@EnableSwagger2Doc
public class FemasApplication {

    private static final Logger log = LoggerFactory.getLogger(FemasApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FemasApplication.class, args);
        try {
            startRpcServer();
            startThriftServer();
        } catch (IOException e) {
            log.error("start grpc fail", e);
        } catch (TTransportException e) {
            log.error("start thrift fail", e);
        }
    }

    private static void startRpcServer() throws IOException {
        int grpcPort = GrpcHepler.getGrpcListenPort();
        NettyServerBuilder.forPort(grpcPort)
                .addService(new GrpcLongPollingEndpoint())
                .build()
                .start();
        log.info("start grpc on port:{}", grpcPort);
    }

    private static void startThriftServer() throws TTransportException, IOException {
        int thriftListenPort = ThriftHelper.getThriftListenPort();
        TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(thriftListenPort);
        //参数设置
        THsHaServer.Args arg = new THsHaServer.Args(serverSocket).minWorkerThreads(2).maxWorkerThreads(4);
        //处理器
        PaasPolling.Processor<PaasPolling.Iface> processor = new PaasPolling.Processor<>(new ThriftLongPollingEndpoint());
        arg.protocolFactory(new TCompactProtocol.Factory());
        arg.transportFactory(new TFramedTransport.Factory());
        arg.processorFactory(new TProcessorFactory(processor));
        TServer server = new TNonblockingServer(arg);
        server.serve();
    }
}
