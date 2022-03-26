package com.tencent.tsf.femas.common.monitor;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author willJo
 * @since 2022/3/25
 */
public class EndpointTest extends TestCase {

    @Test
    public void test01() {
        Endpoint endpoint = getEndpoint("order", "query");
        endpoint.setIpv4("ip");
        endpoint.setPort("8080");


        Endpoint endpoint1 = getEndpoint("order", "query");

        Map<Endpoint, String> map = new HashMap<>();
        map.put(endpoint, "123");
        map.put(endpoint1, "123");
        Assert.assertTrue(endpoint.equals(endpoint1));
        Assert.assertEquals(map.size(), 1);
    }



    private Endpoint getEndpoint(String serviceName, String interfaceName) {
        Endpoint endpoint1 = new Endpoint();
        endpoint1.setInterfaceName(interfaceName);
        endpoint1.setServiceName(serviceName);
        return endpoint1;
    }


}