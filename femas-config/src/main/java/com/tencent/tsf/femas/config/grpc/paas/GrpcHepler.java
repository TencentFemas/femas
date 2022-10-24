package com.tencent.tsf.femas.config.grpc.paas;

import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.FemasConfig;

import java.net.URI;

/**
 * @ClassName GrpcAdminSelector
 * @Description TODO
 * @Author cstongwei
 * @Date 2022/5/30 16:49
 * @Version 1.0.0
 **/
public class GrpcHepler {

    final static String SUCC = "0";
    final static String FAIL = "-1";
    final static String DEFAULT_GRPC_ADDRESS = "localhost";
    final static int DEFAULT_GRPC_PORT = 5555;
    static final String PAAS_SERVER_ADDRESS = "paas_server_address";
    static final String PAAS_GRPC_PORT = "paas_grpc_port";
    static final String LISTEN_GRPC_PORT = "grpc_port";

    public static String getPaasServerHost(){
        String domain = FemasConfig.getProperty(PAAS_SERVER_ADDRESS);
        if(StringUtils.isBlank(domain)){
            return DEFAULT_GRPC_ADDRESS;
        }
        URI  url = URI.create(domain);
        return url.getHost();
    }

    public static int getPaasGrpcPort(){
        String grpcPort = FemasConfig.getProperty(PAAS_GRPC_PORT);
        if(StringUtils.isBlank(grpcPort)){
            return DEFAULT_GRPC_PORT;
        }
        return Integer.valueOf(grpcPort);
    }

    public static int getGrpcListenPort(){
        String grpcPort = FemasConfig.getProperty(LISTEN_GRPC_PORT);
        if(StringUtils.isBlank(grpcPort)){
            return DEFAULT_GRPC_PORT;
        }
        return Integer.valueOf(grpcPort);
    }

    public static PollingResult ok(){
       return PollingResult.newBuilder().setCode(SUCC).build();
    }

    public static PollingResult ok(String result){
        return PollingResult.newBuilder().setCode(SUCC).setResult(result).build();
    }

    public static PollingResult fail(){
        return PollingResult.newBuilder().setCode(FAIL).build();
    }
    public static PollingResult fail(String message){
        return PollingResult.newBuilder().setCode(FAIL).setMessage(message).build();
    }

    public static boolean success(PollingResult pollingResult){
        return pollingResult!=null && StringUtils.equals(pollingResult.getCode(), SUCC);
    }
}
