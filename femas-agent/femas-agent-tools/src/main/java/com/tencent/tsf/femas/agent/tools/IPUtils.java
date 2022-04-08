package com.tencent.tsf.femas.agent.tools;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPUtils {
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        //返回站点本地地址
                        if (ip.isSiteLocalAddress() && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            AgentLogger.getLogger().severe("本机IP地址获取失败，{}" + AgentLogger.getStackTraceString(e));
        }
        return "";
    }

    public static List<String> getIpAddressList() {
        List<String> addressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        //返回站点本地地址
                        if (ip.isSiteLocalAddress() && ip instanceof Inet4Address) {
                            addressList.add(ip.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            AgentLogger.getLogger().severe("本机IP地址获取失败，{}" + AgentLogger.getStackTraceString(e));
        }
        return addressList;
    }

}
