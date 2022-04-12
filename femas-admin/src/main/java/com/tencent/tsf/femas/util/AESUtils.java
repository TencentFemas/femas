package com.tencent.tsf.femas.util;


import com.tencent.tsf.femas.common.util.StringUtils;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密和解密工具类
 */
public class AESUtils {

    /**
     * 签名算法
     */
    public static final String SIGN_ALGORITHMS = "SHA1PRNG";
    public static final String KEY = "alkjzmsnsvasdf";
    /**
     * 编码格式
     */
    private static final String CHARSET_UTF8 = "UTF-8";
    /**
     * AES加密算法
     */
    private static final String KEY_AES = "AES";

    public static void main(String[] args) throws Exception {
        String encrypt = encrypt("234234234{}");
        System.out.println(encrypt);
        //testDecrypt();
    }


    /**
     * AES加密
     *
     * @param data 需要加密的内容
     * @return
     */
    public static String encrypt(String data) {
        try {
            return doAES(data, KEY, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES解密
     *
     * @param data 待解密内容
     * @return
     */
    public static String decrypt(String data) {
        try {
            return doAES(data, KEY, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加解密
     *
     * @param data 待处理数据
     * @param key 密钥
     * @param mode 加解密mode
     * @return
     */
    private static String doAES(String data, String key, int mode) throws Exception {
        try {
            if (StringUtils.isBlank(data) || StringUtils.isBlank(key)) {
                return null;
            }
            //判断是加密还是解密
            boolean encrypt = mode == Cipher.ENCRYPT_MODE;
            byte[] content;
            // true 加密内容 false 解密内容
            if (encrypt) {
                content = data.getBytes(CHARSET_UTF8);
            } else {
                content = parseHexStr2Byte(data);
            }
            // 1.构造密钥生成器，指定为AES算法,不区分大小写
            KeyGenerator kgen = KeyGenerator.getInstance(KEY_AES);
            // 2.根据ecnodeRules规则初始化密钥生成器

            // 生成一个128位的随机源,根据传入的字节数组
            //kgen.init(128, new SecureRandom(key.getBytes()));
            //生成一个128位的随机源,根据传入的字节数组,防止linux下 随机生成key
            SecureRandom random = SecureRandom.getInstance(SIGN_ALGORITHMS);
            random.setSeed(key.getBytes(CHARSET_UTF8));
            kgen.init(128, random);

            // 3.产生原始对称密钥
            SecretKey secretKey = kgen.generateKey();
            // 4.获得原始对称密钥的字节数组
            byte[] enCodeFormat = secretKey.getEncoded();
            // 5.根据字节数组生成AES密钥
            SecretKeySpec keySpec = new SecretKeySpec(enCodeFormat, KEY_AES);
            // 6.根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance(KEY_AES);// 创建密码器
            // 7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
            cipher.init(mode, keySpec);// 初始化
            byte[] result = cipher.doFinal(content);
            if (encrypt) {
                // 将二进制转换成16进制
                return parseByte2HexStr(result);
            } else {
                return new String(result, CHARSET_UTF8);
            }
        } catch (Exception e) {
            throw new Exception("AES 密文处理异常:" + e.getMessage());
        }
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


}
