package com.tencent.tsf.femas.common.util;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

/**
 * @ClassName IPUtils
 * @Author Leo
 * @Description //TODO
 **/

public class IPUtils {

    public static String getIpAddress() {
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            ArrayList<NetworkInterface> interfaces = Collections.list(networkInterfaces);
            interfaces.sort(new Comparator<NetworkInterface>() {
                @Override
                public int compare(NetworkInterface o1, NetworkInterface o2) {
                    return o1.getIndex() - o2.getIndex();
                }
            });
            for (NetworkInterface iface : interfaces) {
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr.getHostAddress();
                        }
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress == null ? InetAddress.getLocalHost().getHostAddress()
                    : candidateAddress.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
