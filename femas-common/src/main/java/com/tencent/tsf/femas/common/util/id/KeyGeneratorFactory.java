package com.tencent.tsf.femas.common.util.id;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/31 15:13
 * @Version 1.0
 */
public class KeyGeneratorFactory {

    public static KeyGenerator gen(GeneratorEnum type) {
        if (GeneratorEnum.HOST.equals(type)) {
            return new HostNameKeyGenerator();
        }
        if (GeneratorEnum.IP.equals(type)) {
            return new IPKeyGenerator();
        }
        if (GeneratorEnum.IP_SECTION.equals(type)) {
            return new IPSectionKeyGenerator();
        }
        return new IPKeyGenerator();
    }

}
