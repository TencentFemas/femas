package com.tencent.tsf.femas.common.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressUtils {

    public static final String DEFAULT_LOCAL_HOST_ADDRESS = "127.0.0.1";
    private static final Logger logger = LoggerFactory.getLogger(AddressUtils.class);
    private static volatile String localHostAddress;


    public static String getValidLocalHost() {
        if (localHostAddress == null) {
            synchronized (AddressUtils.class) {
                if (localHostAddress == null) {
                    InetAddress inetAddress = findFirstNonLoopbackAddress();
                    if (inetAddress != null && StringUtils.isNotEmpty(inetAddress.getHostAddress())) {
                        localHostAddress = inetAddress.getHostAddress();
                    } else {
                        localHostAddress = DEFAULT_LOCAL_HOST_ADDRESS;
                    }
                }
            }
        }
        return localHostAddress;
    }

    /**
     * from org.springframework.cloud.commons.util.InetUtils
     *
     * @return
     */
    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface
                    .getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    logger.trace("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest) {
                        lowest = ifc.getIndex();
                    } else if (result != null) {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc
                            .getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address
                                && !address.isLoopbackAddress()) {
                            logger.trace("Found non-loopback interface: "
                                    + ifc.getDisplayName());
                            result = address;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.warn("Unable to retrieve localhost");
        }

        return null;
    }

}
