package com.tencent.tsf.femas.common.monitor;

import java.util.Objects;

public class Endpoint {

    private String serviceName;

    private String interfaceName;

    private String ipv4;

    private String port;

    public Endpoint() {

    }

    public Endpoint(String serviceName, String interfaceName) {
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
    }

    public Endpoint(String serviceName, String interfaceName, String ipv4, String port) {
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        this.ipv4 = ipv4;
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, interfaceName);
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Endpoint) {
            Endpoint that = (Endpoint) o;
            if (null != this.serviceName && null != that.serviceName) {
                if (null != this.interfaceName && null != that.interfaceName) {
                    return this.serviceName.equals(that.serviceName) && this.interfaceName.equals(that.interfaceName);
                } else {
                    return this.serviceName.equals(that.serviceName);
                }
            } else if (null != this.interfaceName && null != that.interfaceName) {
                return this.serviceName.equals(that.serviceName);
            } else if (null == this.serviceName && null == that.serviceName && null == this.interfaceName
                    && null == that.interfaceName) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "InvocationEndpoint{" +
                "serviceName='" + serviceName + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", ipv4='" + ipv4 + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}