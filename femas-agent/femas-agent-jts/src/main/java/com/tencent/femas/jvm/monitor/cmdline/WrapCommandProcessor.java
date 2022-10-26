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

package com.tencent.femas.jvm.monitor.cmdline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.femas.jvm.monitor.jvmmonitoragent.JvmMonitorAgentEntrance;
import com.tencent.femas.jvm.monitor.utils.Command;
import com.tencent.femas.jvm.monitor.utils.Logger;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import static java.net.HttpURLConnection.HTTP_OK;

public class WrapCommandProcessor {
    private static final Logger LOGGER = Logger.getLogger(WrapCommandProcessor.class);
    private static ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CloseableHttpClient httpClient;

    static {
        httpClient = CmdLauncher.getHttpClient();
    }

    public static String processCommand(String cmdString) {
        String ret;
        // TODO: preprocess and postprocess.
        long enterTime = System.currentTimeMillis();
        if (cmdString.equalsIgnoreCase(
                "{\"taskId\":\"Qoco_getPid\",\"type\":\"getpid\",\"action\":\"\",\"metaInfo\":\"\"}")) {
            LOGGER.debug("enter: process command internal: " + cmdString);
        } else {
            LOGGER.info("enter: process command internal: " + cmdString);
        }

        ret = processCommandInternal(cmdString);

        if (cmdString.equalsIgnoreCase(
                "{\"taskId\":\"Qoco_getPid\",\"type\":\"getpid\",\"action\":\"\",\"metaInfo\":\"\"}")) {
            LOGGER.debug("leave: process command internal: " + cmdString
                    + " duration: " + (System.currentTimeMillis() - enterTime)
                    + " ms.");
        } else {
            LOGGER.info("leave: process command internal: " + cmdString
                    + " duration: " + (System.currentTimeMillis() - enterTime)
                    + " ms.");
        }
        LOGGER.debug("process command return!!" + ret);
        return ret;
    }

    private static String processCommandInternal(String cmdString) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        Command cmd;

        // In case of throw json exception again, directly use fail String here.

        // null for 400
        String failConnRespString = null;

        try {
            LOGGER.debug("Process Command: " + cmdString);
          // mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            cmd = mapper.readValue(cmdString, Command.class);
            // taskMap.put(cmd.getTaskID(), new TaskInfo(cmd, TaskStatus.BUSY));
        } catch (Exception e) {
            e.printStackTrace();
            // taskMap.put(cmd.getTaskID(), new TaskInfo(null, TaskStatus.ERROR));
            LOGGER.error("Illegal request format:" + cmdString);
            return null;
        }

        // TODO. also record status?
        if (cmd == null) {
            LOGGER.error("Illegal request format:" + cmdString);
            return null;
        }

        // Now we can get task ID.
        failConnRespString = "{\"taskId\":\"" + cmd.getTaskId()
                + "\",\"resultInfo\":{\"status\":\"ERROR\",\"statusInfo\":\"NO_CONNECTION\",\"data\":"
                + "\"Connection issue\"}}";

        String resultString = "";
        String url = "http://" + JvmMonitorArgs.getInstance().getHotspotIp() + ":"
                + JvmMonitorArgs.getInstance().getHotspotPort() + JvmMonitorAgentEntrance.JDK_CTX;
        try {
            // 创建Http Post请求
            LOGGER.debug("send post request to " + url + " command: " + cmdString);
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(cmdString);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HTTP_OK) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
                LOGGER.debug("response from hotspot: cmd: <" + cmdString + ">, response: <" + resultString + ">");
            } else {
                throw new Exception("Error response code: " + statusCode);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot get response from hotspot process!");
            e.printStackTrace();
            resultString = failConnRespString;
            LOGGER.debug("return resultString with null");
        } finally {
            try {
                LOGGER.debug("close response" + response);
                if (response != null) {
                    response.close();
                }
                LOGGER.debug("close response finish");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOGGER.debug("fail close response");
                e.printStackTrace();
            }
        }
        LOGGER.debug("return resultString with null!!");
        return resultString;
    }
}
