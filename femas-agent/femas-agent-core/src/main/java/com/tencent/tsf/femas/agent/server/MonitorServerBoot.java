/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.tsf.femas.agent.server;

import com.tencent.tsf.femas.agent.classloader.AgentClassLoader;
import com.tencent.tsf.femas.agent.classloader.AgentPackagePathScanner;
import com.tencent.tsf.femas.agent.classloader.JvmMonitorClassloader;
import com.tencent.tsf.femas.agent.tools.AgentLogger;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/9/22 16:48
 */
public class MonitorServerBoot implements ServerBoot {

    private static final AgentLogger LOGGER = AgentLogger.getLogger(MonitorServerBoot.class);

    private static final String QOCO_CLASS_NAME = "com.tencent.femas.jvm.monitor.jvmmonitoragent.JvmMonitorAgentEntrance";
    private static final String QOCO_MAIN_METHOD = "main";
    private static final String QOCO_PROFILERAGENTMAIN_METHOD = "profilerAgentMain";
    // must be same as DEFAULT_PORT in JvmMonitorAgentEntrance, but we dont want to load it.
    private static final String DEFAULT_PORT = "11099";
    // must be same as JDK_CTX in JvmMonitorAgentEntrance
    public static final String JDK_CTX = "/jvm";
    private static boolean alreadyLoaded = false;
    private static Instrumentation instrumentation;
    private static Class<?> qClass = null;
    private static JvmMonitorClassloader monitorClassLoader = null;
    private static final String PARENT_DIR_JTS = "jts";

    @Override
    public void init(Object... context) {
        for (Object var : context) {
            if (var instanceof Instrumentation) {
                instrumentation = (Instrumentation) var;
            }
        }
        try {
            String classPath = System.getProperty("java.class.path");
            String[] cpArr = classPath.split(":");
            int len = cpArr.length;
            URL[] urls = new URL[len];
            for (int i = 0; i < len; i++) {
                urls[i] = Paths.get(cpArr[i]).toUri().toURL();
            }
            monitorClassLoader = new JvmMonitorClassloader(urls);
            qClass = monitorClassLoader.loadClass(QOCO_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            LOGGER.error("MonitorServerBoot init failed", e);
        } catch (MalformedURLException e) {
            LOGGER.error("Fail create URL for method " + QOCO_MAIN_METHOD);
        }
    }

    private static String extractPortNum(String options) {
        if (options == null || options.length() == 0) {
            return DEFAULT_PORT;
        }
        String[] opts = options.split(";");

        for (String kvPair : opts) {
            // TODO. warning if fillOption returns false?
            if (kvPair.startsWith("portNum=")) {
                String[] kv = kvPair.split("=");
                if (kv.length != 2) {
                    return DEFAULT_PORT;
                } else {
                    return kv[1];
                }
            }
        }
        return DEFAULT_PORT;
    }

    @Override
    public void startup(String options) {
        if (alreadyLoaded != true) {
            alreadyLoaded = true;
        } else {
            return;
        }
        String version = MonitorServerBoot.class.getPackage().getImplementationVersion();
        LOGGER.info("Agent VERSION: " + version);
        String portNum = extractPortNum(options);
        // For compatibility.
        if (maybeInUse(portNum)) {
            LOGGER.warn("JvmMonitor port (" + portNum + ") already in use, caused by duplication of JVM monitor agent");
            return;
        }
        try {
            Method mainMethod = qClass.getMethod(QOCO_MAIN_METHOD, String.class, Instrumentation.class);
            mainMethod.invoke(null, options, instrumentation);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Fail find method " + QOCO_MAIN_METHOD);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOGGER.error("Fail invoke method " + QOCO_MAIN_METHOD);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            LOGGER.error("Fail invethod " + QOCO_MAIN_METHOD);
            e.printStackTrace();
        }
    }

    private static boolean maybeInUse(String portNum) {
        // Test whether TencentCloudJvmMonitor exist more than once.
        // if yes, testing port...
        if (dupInArguments("TencentCloudJvmMonitor")) {
            LOGGER.warn("multiple TecnentCloudJvmMonitor agent found, processing... it may take several seconds");
            // Test whether port is in use.
            if (portInUse(portNum)) {
                LOGGER.warn("jvm monitor port (" + portNum + ") already in Use, duplicated JvmMonitor agent?");
                return true;
            }
        }
        return false;
    }

    private static boolean dupInArguments(String agentName) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> args = bean.getInputArguments();
        Iterator iter = args.iterator();
        boolean dup = false;
        boolean exist = false;
        while (iter.hasNext()) {
            String arg = (String) iter.next();
            LOGGER.info("Argumnets: " + arg);
            if (arg.contains(agentName)) {
                if (exist) {
                    LOGGER.warn("Found dupilicated JvmMonitor agents");
                    dup = true;
                } else {
                    exist = true;
                }
            }
        }
        return dup;
    }

    private static boolean portInUse(String port) {
        //testing by send command to self
        String url = "http://localhost:" + port + JDK_CTX;
        String cmdString = "{\"taskId\":\"Qoco_getPid\",\"type\":\"getpid\",\"action\":\"\",\"metaInfo\":\"\"}";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            StringEntity entity = new StringEntity(cmdString);
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                LOGGER.info("test port response no-OK code: " + response.getStatusLine().getStatusCode());
                return false;
            }
        } catch (Exception e) {
            LOGGER.info(url + " port is not used: exception " + e);
            return false;
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                LOGGER.warn("http client for port testing can not be closed safely");
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
