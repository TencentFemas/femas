package com.tencent.tsf.femas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Cody
 * @date 2021 2021/9/23 5:43 下午
 *         读取resource下的文件内容
 */
public class ResourceFileReadUtil {

    /**
     * @param relativePath 相对路径(不需要加 /)
     * @return
     */
    public static String getResourceAsString(String relativePath) {
        String str = "";
        try {
            String tempStr = "";
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((tempStr = br.readLine()) != null) {
                str = str + tempStr;
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
