package com.tencent.tsf.femas.common.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author willJo
 * @since 2022/3/25
 */
public class ErrorStatusTest extends TestCase {

    @Test
    public void test(){
        ErrorStatus errorStatus = new ErrorStatus(ErrorStatus.Code.OK);
        ErrorStatus errorStatus1 = new ErrorStatus(ErrorStatus.Code.OK);

        Map<ErrorStatus, String> map = new HashMap<>();
        map.put(errorStatus, "aa");
        map.put(errorStatus1, "aa");

        Assert.assertTrue(errorStatus.equals(errorStatus1));

        Assert.assertEquals(map.size(), 1);
    }

}