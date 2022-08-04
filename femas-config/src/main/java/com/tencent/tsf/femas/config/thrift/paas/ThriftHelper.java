package com.tencent.tsf.femas.config.thrift.paas;

import com.tencent.tsf.femas.common.util.StringUtils;
import com.tencent.tsf.femas.config.FemasConfig;

import java.net.URI;

/**
 * @author huyuanxin
 */
public class ThriftHelper {

    private ThriftHelper() {
    }

    static final String SUCCESS = "0";
    static final String FAIL = "-1";

    static final String SUCCESS_MESSAGE = "SUCCESS";

    static final String FAIL_MESSAGE = "FAIL";

    static final int DEFAULT_THRIFT_PORT = 5554;
    static final String PAAS_SERVER_ADDRESS = "paas_server_address";
    static final String PAAS_GRPC_PORT = "paas_thrift_port";
    static final String LISTEN_GRPC_PORT = "thrift_port";

    public static String getPaasServerHost() {
        String domain = FemasConfig.getProperty(PAAS_SERVER_ADDRESS);
        if (StringUtils.isBlank(domain)) {
            return null;
        }
        URI url = URI.create(domain);
        return url.getHost();
    }

    public static int getPaasGrpcPort() {
        String grpcPort = FemasConfig.getProperty(PAAS_GRPC_PORT);
        if (StringUtils.isBlank(grpcPort)) {
            return DEFAULT_THRIFT_PORT;
        }
        return Integer.parseInt(grpcPort);
    }

    public static int getThriftListenPort() {
        String grpcPort = FemasConfig.getProperty(LISTEN_GRPC_PORT);
        if (StringUtils.isBlank(grpcPort)) {
            return DEFAULT_THRIFT_PORT;
        }
        return Integer.parseInt(grpcPort);
    }

    public static com.tencent.tsf.femas.config.thrift.paas.PollingResult ok() {
        PollingResult pollingResult = new PollingResult();
        pollingResult.setCode(SUCCESS);
        pollingResult.setMessage(SUCCESS_MESSAGE);
        pollingResult.setResult(SUCCESS_MESSAGE);
        return pollingResult;
    }

    public static com.tencent.tsf.femas.config.thrift.paas.PollingResult ok(String result) {
        PollingResult pollingResult = new PollingResult();
        pollingResult.setCode(SUCCESS);
        pollingResult.setResult(result);
        pollingResult.setMessage(SUCCESS_MESSAGE);
        return pollingResult;
    }

    public static com.tencent.tsf.femas.config.thrift.paas.PollingResult fail() {
        PollingResult pollingResult = new PollingResult();
        pollingResult.setCode(FAIL);
        pollingResult.setMessage(FAIL_MESSAGE);
        pollingResult.setResult(FAIL_MESSAGE);
        return pollingResult;
    }

    public static com.tencent.tsf.femas.config.thrift.paas.PollingResult fail(String message) {
        PollingResult pollingResult = new PollingResult();
        pollingResult.setCode(FAIL);
        pollingResult.setMessage(message);
        pollingResult.setResult(message);
        return pollingResult;
    }

    public static boolean success(PollingResult pollingResult) {
        return pollingResult != null && StringUtils.equals(pollingResult.getCode(), SUCCESS);
    }
}
