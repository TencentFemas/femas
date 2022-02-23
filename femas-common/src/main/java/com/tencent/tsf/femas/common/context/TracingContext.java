package com.tencent.tsf.femas.common.context;

public class TracingContext {

    private String localServiceName;

    private String localInterface;

    private String localIpv4;

    private Integer localPort;

    private String protocol;

    private String resultStatus;

    /**
     * 服务端服务名
     */
    private String remoteServiceName;
    /**
     * 服务端接口
     */
    private String remoteInterface;
    /**
     * 服务端IP V4
     */
    private String remoteIpv4;
    /**
     * 服务端端口号
     */
    private Integer remotePort;
    /**
     * 开始时间（微秒）
     */
    private Long startTime;
    /**
     * 结束时间（微秒）
     */
    private Long finishTime;

    private String localHttpMethod;

    private String remoteHttpMethod;

    private String localApplicationVersion;

    private String localNamespaceId;

    private String remoteApplicationVersion;

    private String remoteNamespaceId;

    private String localInstanceId;

    private String remoteInstanceId;

    public String getLocalServiceName() {
        return localServiceName;
    }

    public void setLocalServiceName(String localServiceName) {
        this.localServiceName = localServiceName;
    }

    public String getLocalInterface() {
        return localInterface;
    }

    public void setLocalInterface(String localInterface) {
        this.localInterface = localInterface;
    }

    public String getLocalIpv4() {
        return localIpv4;
    }

    public void setLocalIpv4(String localIpv4) {
        this.localIpv4 = localIpv4;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getRemoteServiceName() {
        return remoteServiceName;
    }

    public void setRemoteServiceName(String remoteServiceName) {
        this.remoteServiceName = remoteServiceName;
    }

    public String getRemoteInterface() {
        return remoteInterface;
    }

    public void setRemoteInterface(String remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    public String getRemoteIpv4() {
        return remoteIpv4;
    }

    public void setRemoteIpv4(String remoteIpv4) {
        this.remoteIpv4 = remoteIpv4;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public String getLocalHttpMethod() {
        return localHttpMethod;
    }

    public void setLocalHttpMethod(String localHttpMethod) {
        this.localHttpMethod = localHttpMethod;
    }

    public String getLocalApplicationVersion() {
        return localApplicationVersion;
    }

    public void setLocalApplicationVersion(String localApplicationVersion) {
        this.localApplicationVersion = localApplicationVersion;
    }

    public String getRemoteHttpMethod() {
        return remoteHttpMethod;
    }

    public void setRemoteHttpMethod(String remoteHttpMethod) {
        this.remoteHttpMethod = remoteHttpMethod;
    }

    public String getLocalNamespaceId() {
        return localNamespaceId;
    }

    public void setLocalNamespaceId(String localNamespaceId) {
        this.localNamespaceId = localNamespaceId;
    }

    public String getRemoteApplicationVersion() {
        return remoteApplicationVersion;
    }

    public void setRemoteApplicationVersion(String remoteApplicationVersion) {
        this.remoteApplicationVersion = remoteApplicationVersion;
    }

    public String getRemoteNamespaceId() {
        return remoteNamespaceId;
    }

    public void setRemoteNamespaceId(String remoteNamespaceId) {
        this.remoteNamespaceId = remoteNamespaceId;
    }

    public String getLocalInstanceId() {
        return localInstanceId;
    }

    public void setLocalInstanceId(String localInstanceId) {
        this.localInstanceId = localInstanceId;
    }

    public String getRemoteInstanceId() {
        return remoteInstanceId;
    }

    public void setRemoteInstanceId(String remoteInstanceId) {
        this.remoteInstanceId = remoteInstanceId;
    }
}
