///**
// * Copyright 2010-2021 the original author or authors
// *
// * Permission is hereby granted, free of charge, to any person obtaining
// * a copy of this software and associated documentation files (the
// * "Software"), to deal in the Software without restriction, including
// * without limitation the rights to use, copy, modify, merge, publish,
// * distribute, sublicense, and/or sell copies of the Software, and to
// * permit persons to whom the Software is furnished to do so, subject to
// * the following conditions:
// *
// * The above copyright notice and this permission notice shall be
// * included in all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package com.tencent.femas.jvm.monitor.jvmmonitoragent;
//
//import com.tencent.femas.jvm.monitor.utils.Logger;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//
//import java.io.IOException;
//import java.lang.instrument.Instrumentation;
//import java.lang.management.ManagementFactory;
//import java.lang.management.RuntimeMXBean;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.file.Paths;
//import java.util.Iterator;
//import java.util.List;
//
//public class TencentJvmMonitorAgent {
//    private static final String QOCO_CLASS_NAME =
//            "com.tencent.femas.jvm.monitor.jvmmonitoragent.JvmMonitorAgentEntrance";
//    private static final String QOCO_MAIN_METHOD = "main";
//    private static final String QOCO_PROFILERAGENTMAIN_METHOD = "profilerAgentMain";
//    private static final Logger LOGGER = Logger.getLogger(TencentJvmMonitorAgent.class);
//    // must be same as DEFAULT_PORT in JvmMonitorAgentEntrance, but we dont want to load it.
//    private static final String DEFAULT_PORT = "11339";
//    // must be same as JDK_CTX in JvmMonitorAgentEntrance
//    public static final String JDK_CTX = "/jvm";
//    private static boolean alreadyLoaded = false;
//    private static JvmMonitorClassloader qocoLoader = null;
//    private static Class<?> qClass = null;
//
//    static {
//        try {
//            LOGGER.debug("class path: " + System.getProperty("java.class.path")
//                    + " Loader: " + TencentJvmMonitorAgent.class.getClassLoader());
//            String classPath = System.getProperty("java.class.path");
//            String[] cpArr = classPath.split(":");
//            int len = cpArr.length;
//            URL[] urls = new URL[len];
//            for (int i = 0; i < len; i++) {
//                urls[i] = Paths.get(cpArr[i]).toUri().toURL();
//            }
//            qocoLoader = new JvmMonitorClassloader(urls);
//            // Thread.currentThread().setContextClassLoader(qocoLoader);
//            qClass = qocoLoader.loadClass(QOCO_CLASS_NAME);
//        } catch (ClassNotFoundException e) {
//            LOGGER.error("Fail load class " + QOCO_CLASS_NAME);
//            e.printStackTrace();
//        } catch (MalformedURLException e) {
//            LOGGER.error("Fail create URL for method " + QOCO_MAIN_METHOD);
//            e.printStackTrace();
//        }
//    }
//
//    public static JvmMonitorClassloader getQocoLoader() {
//        return qocoLoader;
//    }
//
//    public static void premain(String options, Instrumentation inst) {
//        //main(options, inst);
//        if (alreadyLoaded != true) {
//            alreadyLoaded = true;
//        } else {
//            return;
//        }
//
//        LOGGER.debug("premain options: " + options);
//        String version = TencentJvmMonitorAgent.class.getPackage().getImplementationVersion();
//        LOGGER.info("Agent VERSION: " + version);
//        String portNum = extractPortNum(options);
//        LOGGER.debug("testing JvmMonitoring port " + portNum);
//
//        // For compatibility.
//        if (maybeInUse(portNum)) {
//            LOGGER.warn("JvmMonitor port (" + portNum + ") already in use, caused by duplication of JVM monitor agent");
//            return;
//        }
//
//        try {
//            Method mainMethod = qClass.getMethod(QOCO_MAIN_METHOD, String.class, Instrumentation.class);
//            mainMethod.invoke(null, options, inst);
//        } catch (NoSuchMethodException e) {
//            LOGGER.error("Fail find method " + QOCO_MAIN_METHOD);
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            LOGGER.error("Fail invoke method " + QOCO_MAIN_METHOD);
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            LOGGER.error("Fail invethod " + QOCO_MAIN_METHOD);
//            e.printStackTrace();
//        }
//    }
//
//    private static String extractPortNum(String options) {
//        if (options == null || options.length() == 0) {
//            return DEFAULT_PORT;
//        }
//        String[] opts = options.split(";");
//
//        for (String kvPair : opts) {
//            // TODO. warning if fillOption returns false?
//            if (kvPair.startsWith("portNum=")) {
//                String[] kv = kvPair.split("=");
//                if (kv.length != 2) {
//                    return DEFAULT_PORT;
//                } else {
//                    return kv[1];
//                }
//            }
//        }
//        return DEFAULT_PORT;
//    }
//
//
//    // TODO - implement attach?
//    public static void agentmain(String options, Instrumentation inst) {
//        AgentMainOptions mainOptions = new AgentMainOptions(options);
//        if (mainOptions.activateJvmMonitor) {
//            // TODO: implement the start/stop of jvm monitor
//            premain(options, inst);
//        }
//        if (mainOptions.activateJvmProfiler) {
//            try {
//                Method profilerAgentMain = qClass.getMethod(QOCO_PROFILERAGENTMAIN_METHOD);
//                profilerAgentMain.setAccessible(true);
//
//                profilerAgentMain.invoke(null, mainOptions.methodsToBeTraced, inst);
//            } catch (IllegalAccessException e) {
//                LOGGER.error("Fail invoke method " + QOCO_PROFILERAGENTMAIN_METHOD);
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                LOGGER.error("Fail invoke method " + QOCO_PROFILERAGENTMAIN_METHOD);
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                LOGGER.error("Can not method " + QOCO_PROFILERAGENTMAIN_METHOD);
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static boolean maybeInUse(String portNum) {
//        // Test whether TencentCloudJvmMonitor exist more than once.
//        // if yes, testing port...
//        if (dupInArguments("TencentCloudJvmMonitor")) {
//            LOGGER.warn("multiple TecnentCloudJvmMonitor agent found, processing... it may take several seconds");
//            // Test whether port is in use.
//            if (portInUse(portNum)) {
//                LOGGER.warn("jvm monitor port (" + portNum + ") already in Use, duplicated JvmMonitor agent?");
//                return true;
//            }
//        }
//        return false;
//    }
//
//    // following code not work because of class loading
//    private static boolean dupInArguments(String agentName) {
//        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
//        List<String> args = bean.getInputArguments();
//        Iterator iter = args.iterator();
//        boolean dup = false;
//        boolean exist = false;
//        while (iter.hasNext()) {
//            String arg = (String) iter.next();
//            LOGGER.debug("Argumnets: " + arg);
//            if (arg.contains(agentName)) {
//                if (exist) {
//                    LOGGER.warn("Found dupilicated JvmMonitor agents");
//                    dup = true;
//                } else {
//                    exist = true;
//                }
//            }
//        }
//         return dup;
//    }
//
//    private static boolean portInUse(String port) {
//        //testing by send command to self
//        String url = "http://localhost:" + port + JDK_CTX;
//        String cmdString = "{\"taskId\":\"Qoco_getPid\",\"type\":\"getpid\",\"action\":\"\",\"metaInfo\":\"\"}";
//        CloseableHttpClient client = HttpClients.createDefault();
//        HttpPost httpPost = new HttpPost(url);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            StringEntity entity = new StringEntity(cmdString);
//            httpPost.setEntity(entity);
//            CloseableHttpResponse response = client.execute(httpPost);
//            if (response.getStatusLine().getStatusCode() == 200) {
//                return true;
//            } else {
//                LOGGER.debug("test port response no-OK code: " + response.getStatusLine().getStatusCode());
//                return false;
//            }
//        } catch (Exception e) {
//            LOGGER.debug(url + " port is not used: exception " +  e);
//            return false;
//        } finally {
//            try {
//                client.close();
//            } catch (IOException e) {
//                LOGGER.warn("http client for port testing can not be closed safely");
//            }
//        }
//    }
//
//    private static class AgentMainOptions {
//        private boolean activateJvmMonitor;
//        private boolean activateJvmProfiler;
//        private String methodsToBeTraced;
//
//        public AgentMainOptions(String options) {
//            // JvmMonitor=on|off
//            reset();
//            processOptions(options);
//        }
//
//        private void processOptions(String options) {
//            if (options == null || options.length() == 0) {
//                return;
//            }
//            String[] opts = options.split(";");
//
//            for (String kvPair : opts) {
//                if (kvPair.startsWith("JvmMonitor=")) {
//                    String[] kv = kvPair.split("=");
//                    if (kv.length != 2) {
//                        reset();
//                        return;
//                    } else {
//                        if (kv[1].equalsIgnoreCase("on")) {
//                            activateJvmMonitor = true;
//                        } else if (kv[1].equalsIgnoreCase("off")) {
//                            activateJvmMonitor = false;
//                        }
//                    }
//                } else if (kvPair.startsWith("JvmProfiler=")) {
//                    String[] kv = kvPair.split("=");
//                    if (kv.length != 2) {
//                        reset();
//                        return;
//                    } else {
//                        if (kv[1].equalsIgnoreCase("on")) {
//                            activateJvmProfiler = true;
//                        } else if (kv[1].equalsIgnoreCase("off")) {
//                            activateJvmProfiler = false;
//                        } else {
//                            LOGGER.error("Invalid option for JvmProfiler= " + kv[1]);
//                            reset();
//                            return;
//                        }
//                    }
//                } else if (kvPair.startsWith("TraceMethods=")) {
//                    String[] kv = kvPair.split("=");
//                    if (kv.length != 2) {
//                        reset();
//                        return;
//                    } else {
//                        methodsToBeTraced = kv[1];
//                        if (methodsToBeTraced == null || methodsToBeTraced.length() == 0) {
//                            LOGGER.error("No methods specified for TraceMethod=");
//                            reset();
//                            return;
//                        }
//                    }
//                }
//            }
//        }
//
//        private void reset() {
//            activateJvmMonitor = true;
//            activateJvmProfiler = false;
//            methodsToBeTraced = null;
//        }
//
//    }
//}
