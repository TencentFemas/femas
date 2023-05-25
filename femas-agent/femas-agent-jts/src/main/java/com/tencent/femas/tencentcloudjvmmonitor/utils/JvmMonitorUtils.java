/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.tencentcloudjvmmonitor.utils;

import java.io.*;

public class JvmMonitorUtils {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorUtils.class);
    private static String dataSavePath = "/Users/momo/data/tsf_apm/monitor/jvm-metrics/";
    // only enable manually at develop phase
    private static final Boolean DUMP_FILE_CONTENT = false;

    public static String getFileFromJar(String name) throws Exception {
        InputStream in = JvmMonitorUtils.class.getClassLoader().getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        int read = -1;
        File temp = File.createTempFile(name, "");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);

        while ((read = in.read(buffer)) != -1) {
            if (DUMP_FILE_CONTENT) {
                LOGGER.debug("read file (" + name + ") : " + new String(buffer));
            }
            fos.write(buffer, 0, read);
        }
        fos.close();
        in.close();

        return temp.getAbsolutePath();
    }

    public static String getUpdatedFileFromJar(String name, String pathToAdd) throws Exception {
        InputStream in = JvmMonitorUtils.class.getClassLoader().getResourceAsStream(name);
        byte[] buffer = new byte[1024];
        File temp = File.createTempFile(name, "");
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        LOGGER.debug("temp file for jmxtrans config: " + temp.getAbsolutePath());
        while (br.ready()) {
            String line = br.readLine();
            if (DUMP_FILE_CONTENT) {
                LOGGER.debug("read line: " + line);
            }
            if (line.contains("</outputWriter>")) {
                String sep = "/";
                if (pathToAdd.charAt(pathToAdd.length() - 1) == '/') {
                    sep = "";
                }
                String pathString = "<fileName>" + pathToAdd + sep + "jmxtrans-agent.data" + "</fileName>\n";
                LOGGER.debug("Add jmx config file path: " + pathString);
                fos.write(pathString.getBytes(),0, pathString.length());
            }
            fos.write(line.getBytes(), 0, line.length());
        }
        fos.close();
        br.close();
        in.close();
        return temp.getAbsolutePath();
    }


    public static boolean createDataPath() {
        String path = dataSavePath;
        if (path.length() == 0 || path.equalsIgnoreCase("./")) {
            LOGGER.info("Use Data Dir: " + path);
            return true;
        } else {
            try {
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } catch (Exception e) {
                LOGGER.error("Fail create data dir: " + path + e);
                return false;
            }
        }
        LOGGER.info("Use Data Dir: " + path);
        return true;
    }

    public static String getDataSavePath() {
        LOGGER.debug("get Data Save path: " + dataSavePath + " loader "
                + JvmMonitorUtils.class.getClassLoader().toString());
        if (dataSavePath == null) {
            return "";
        }
        if (dataSavePath.charAt(dataSavePath.length() - 1) != '/') {
            return dataSavePath + "/";
        }
        return dataSavePath;
    }

    public static void setDataSavePath(String dataSavePath) {
        LOGGER.debug("ZLIN - update dataSavePath: " + dataSavePath);
        JvmMonitorUtils.dataSavePath = dataSavePath;
    }
}
