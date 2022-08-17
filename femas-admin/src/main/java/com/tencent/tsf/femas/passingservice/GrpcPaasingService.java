package com.tencent.tsf.femas.passingservice;

import com.tencent.tsf.femas.config.grpc.paas.GrpcHepler;
import com.tencent.tsf.femas.endpoint.configlisten.GrpcLongPollingEndpoint;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huyuanxin
 */
public class GrpcPaasingService implements PaasingService {

    private final Logger logger = LoggerFactory.getLogger(GrpcPaasingService.class);

    @Override
    public void doStart() {
        try {
            int grpcPort = GrpcHepler.getGrpcListenPort();
            NettyServerBuilder.forPort(grpcPort)
                    .addService(new GrpcLongPollingEndpoint())
                    .build()
                    .start();
            logger.info("start grpc on port:{}", grpcPort);
        } catch (Exception e) {
            logger.error("Error with staring grpc paasing server:{0}", e);
        }
    }

    @Override
    public String getType() {
        return "GrpcPaasingService";
    }

}
