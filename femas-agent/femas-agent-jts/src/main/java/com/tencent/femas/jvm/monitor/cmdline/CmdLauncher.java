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
import com.sun.tools.attach.VirtualMachine;
import com.tencent.femas.jvm.monitor.utils.*;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_OK;

public class CmdLauncher {

    private static final Logger LOGGER = Logger.getLogger(CmdLauncher.class);
    private static HttpSocketServer httpServer;
    private static AtomicInteger hotspotPid;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final CloseableHttpClient httpClient;

    static {
        hotspotPid = new AtomicInteger(-1);
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(20);
        cm.setDefaultMaxPerRoute(2);

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /*
     * cmdline Arguments :
     * TencentCloudJvmMonitor attach=<ip:port | pid> mode=<all | debug | monitor> monitor_config=<xxx.xml>
     * attach=<target-ip:port | pid> listen=<ip:port> mode=<all | debug | monitor> monitor_config=<xxx.xml>
     * Target:  JVM agent
     * Listen:  ip:port communicate with TSF Agent
     * mode:  debug means BCI online diagnostic; monitor is jmxtrans based monitoring.
     * monitor_config: location of jmx-trans config files.
     */
    public static void main(String[] args) {
        // TODO： args 区分本地还是远程， 远程暂时只支持jmx
        VirtualMachine vm;
        final JvmMonitorArgs options = JvmMonitorArgs.getInstance().parse(args);

        startListenOnHttp(options.getHttpServerPort());
        if (!JvmMonitorUtils.createDataPath()) {
            LOGGER.error("Fail create folder " + JvmMonitorUtils.getDataSavePath() + " gc log data may lose");
        };
        final int pingIntervalSeconds = Integer.parseInt(options.getPingHotspotInterval());
        LOGGER.info("Start to ping hotspot with interval: " + pingIntervalSeconds + " seconds.");
        if (pingIntervalSeconds > 0) {
            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    String pid = pingHotspotRemotely("getpid");
                    LOGGER.debug("get pid: " + pid);
                    try {
                        if (pid != null && pid != "" && Integer.parseInt(pid) >= 0) {
                            LOGGER.debug("Attached to Hotspot, pid is " + pid);
                            if (hotspotPid.get() == -1 || hotspotPid.get() == Integer.parseInt(pid)) {
                                LOGGER.info("hotspot process keep alive: " + pid);
                            } else {
                                LOGGER.warn("hotspot process restarted! previous pid is " + hotspotPid.get()
                                        + " new process id: " + pid);
                                hotspotPid.set(Integer.parseInt(pid));
                            }
                        } else {
                            LOGGER.error("Fail attach to Hotspot process, retry in "
                                    + pingIntervalSeconds + " seconds ...");
                            if (!JvmMonitorUtils.createDataPath()) {
                                LOGGER.debug("Fail create data folder " + JvmMonitorUtils.getDataSavePath()
                                        + ", PASS");
                            } else {
                                LOGGER.warn("recreated the data folder " + JvmMonitorUtils.getDataSavePath()
                                        + ", data may lost");
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Fail attach to Hotspot process (E), retry in "
                                + pingIntervalSeconds + " seconds ..." + " + E: " + e.toString());
                        // e.printStackTrace();
                        // System.exit(-1);
                    }
                }
            }, 0, pingIntervalSeconds, TimeUnit.SECONDS);
        } else {
            LOGGER.info("pingHotspot disabled");
        }
    }

    private static String pingHotspotRemotely(String cmd) {
        // ping with command to getPid of hotspot process.
        String pid;
        String command = "{\"taskId\":\"Qoco_getPid\",\"type\":\"getpid\",\"action\":\"\",\"metaInfo\":\"\"}";
        /*
        String command = "";
        Command comm = new Command("Qoco_getPid", cmd);
        try {
            command = mapper.writeValueAsString(comm);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
         */

        // query PID
        pid = sendCommandToSelf(command);
        return pid;
    }

    private static String sendCommandToSelf(String cmdString) {
        // CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        String url = "http://localhost:" + JvmMonitorArgs.getInstance().getHttpServerPort() + JvmMonitorArgs.DEFAULT_HTTP_FOLDER;
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
                LOGGER.debug("response from self: cmd: <" + cmdString + ">, response: <" + resultString + ">");
                ResultPackage resultPackage = mapper.readValue(resultString, ResultPackage.class);
                return resultPackage.getResultInfo().getData();
            } else {
                throw new Exception("Error response code: " + statusCode);
            }
        } catch (Exception e) {
            LOGGER.error("Fail send command to self for ping hotspot (E): " + e);
            //e.printStackTrace();
            resultString = "";
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOGGER.error("Fail close response (E): " + e);
                e.printStackTrace();
            }
        }
        return resultString;
    }

    private static void startListenOnHttp(String httpServerPort) {
        LOGGER.debug("controller start listen on: " + httpServerPort);
        httpServer = HttpSocketServer.getInstance(httpServerPort, new CmdHttpRequestHandler());
        try {
            httpServer.start(JvmMonitorArgs.DEFAULT_HTTP_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
