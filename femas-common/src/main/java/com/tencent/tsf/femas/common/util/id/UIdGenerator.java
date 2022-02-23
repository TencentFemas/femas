package com.tencent.tsf.femas.common.util.id;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/6 14:52
 * @Version 1.0
 */
public class UIdGenerator {

    public static String generateUid() {
        //默认IP_SECTION方式
        return String.valueOf(KeyGeneratorFactory.gen(GeneratorEnum.IP_SECTION).generate());
    }

}
