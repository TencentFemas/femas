package com.tencent.tsf.femas;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.tencent.tsf.femas.config.grpc.paas.GrpcHepler;
import com.tencent.tsf.femas.endpoint.configlisten.GrpcLongPollingEndpoint;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
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
        } catch (IOException e) {
            log.error("start grpc fail",e);
        }
    }

    private static void startRpcServer() throws IOException {
        int grpcPort = GrpcHepler.getGrpcListenPort();
        NettyServerBuilder.forPort(grpcPort)
                .addService(new GrpcLongPollingEndpoint())
                .build()
                .start();
        log.info("start grpc on port:{}",grpcPort);
    }
}
