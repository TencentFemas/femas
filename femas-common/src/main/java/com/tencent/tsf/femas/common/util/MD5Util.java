package com.tencent.tsf.femas.common.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <b>功能说明:MD5签名工具类
 */
public class MD5Util {


    /**
     * 私有构造方法,将该工具类设为单例模式.
     */
    private MD5Util() {
    }

    /**
     * 获取加盐后的MD5加密结果
     *
     * @param str
     * @return
     */
    public static final String getSaltMD5(String str) {
        if (str == null || str.trim().length() < 1) {
            str = "";
        }
        try {
            str += "75afccd21c1793dfc51158cd97d7762f";
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(str.getBytes());
            byte mdBytes[] = md.digest();
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < mdBytes.length; i++) {
                int temp;
                if (mdBytes[i] < 0) {
                    temp = 256 + mdBytes[i];
                } else {
                    temp = mdBytes[i];
                }
                if (temp < 16) {
                    hash.append(0);
                }
                hash.append(Integer.toString(temp, 16));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取原生MD5加密结果
     *
     * @param str
     * @return
     */
    public static final String getPlainMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(str.getBytes());
            byte mdBytes[] = md.digest();
            StringBuilder hash = new StringBuilder();
            for (int i = 0; i < mdBytes.length; i++) {
                int temp;
                if (mdBytes[i] < 0) {
                    temp = 256 + mdBytes[i];
                } else {
                    temp = mdBytes[i];
                }
                if (temp < 16) {
                    hash.append("0");
                }
                hash.append(Integer.toString(temp, 16));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getIndex(String str) {
        if (StringUtils.isEmpty(str)) {
            return -1;
        }
        String plainMD5 = getPlainMD5(str);
        return plainMD5.hashCode();
    }

}
