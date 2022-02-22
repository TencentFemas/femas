package com.tencent.tsf.femas.common.util;


import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FileUtils {

    /**
     * 回车符
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * 得到项目所在路径
     *
     * @return 项目所在路径
     */
    public static String getBaseDirName() {
        String fileName = null;
        // 先取classes
        java.net.URL url1 = FileUtils.class.getResource("/");
        if (url1 != null) {
            fileName = url1.getFile();
        } else {
            // 取不到再取lib
            String jarpath = ReflectUtils.getCodeBase(FileUtils.class);
            if (jarpath != null) {
                int sofaidx = jarpath.lastIndexOf("sofa");
                if (sofaidx > -1) { // 如果有sofa开头的jar包
                    fileName = jarpath.substring(0, sofaidx);
                } else {
                    int sepidx = jarpath.lastIndexOf(File.separator);
                    if (sepidx > -1) {
                        fileName = jarpath.substring(0, sepidx + 1);
                    }
                }
            }
        }
        // 将冒号去掉 “/”换成“-”
        if (fileName != null) {
            fileName = fileName.replace(":", "").replace(File.separator, "/")
                    .replace("/", "-");
            if (fileName.startsWith("-")) {
                fileName = fileName.substring(1);
            }
        } else {
            // LOGGER.warn("can not parse webapp baseDir path");
            fileName = "UNKNOW_";
        }
        return fileName;
    }

    /**
     * 得到USER_HOME目录
     *
     * @param base 用户目录下文件夹
     * @return 得到用户目录
     */
    public static String getUserHomeDir(String base) {
        String userhome = System.getProperty("user.home");
        File file = new File(userhome, base);
        if (file.exists()) {
            if (!file.isDirectory()) {
                // LOGGER.error("{} exists, but not directory", file.getAbsolutePath());
                throw new FemasRuntimeException(file.getAbsolutePath() + " exists, but not directory");
            }
        } else {
            file.mkdirs(); // 可能创建不成功
        }
        return file.getAbsolutePath();
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 文件内容
     * @throws IOException 发送IO异常
     */
    public static String file2String(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        FileReader reader = null;
        StringWriter writer = null;
        try {
            reader = new FileReader(file);
            writer = new StringWriter();
            char[] cbuf = new char[1024];
            int len = 0;
            while ((len = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, len);
            }
            return writer.toString();
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * 读取类相对路径内容
     *
     * @param relativePath 相对路径
     * @param encoding     编码
     * @return 文件内容
     * @throws IOException 发送IO异常
     */
    public static String file2String(String relativePath, String encoding) throws IOException {
        InputStream is = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                return null;
            }
            reader = new InputStreamReader(is, encoding);
            bufferedReader = new BufferedReader(reader);
            StringBuilder context = new StringBuilder();
            String lineText;
            while ((lineText = bufferedReader.readLine()) != null) {
                context.append(lineText).append(LINE_SEPARATOR);
            }
            return context.toString();
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * 读取类相对路径内容
     *
     * @param file 文件
     * @return 文件内容（按行）
     * @throws IOException 发送IO异常
     */
    public static List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<String>();
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(file);
            bufferedReader = new BufferedReader(reader);
            String lineText = null;
            while ((lineText = bufferedReader.readLine()) != null) {
                lines.add(lineText);
            }
            return lines;
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * 字符流写文件 较快
     *
     * @param file 文件
     * @param data 数据
     * @return 操作是否成功
     * @throws IOException 发送IO异常
     */
    public static boolean string2File(File file, String data) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file, false);
            writer.write(data);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return true;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 文件夹
     * @return 是否删除完成
     */
    public static boolean cleanDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = cleanDirectory(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }
}