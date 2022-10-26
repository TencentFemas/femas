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

package com.tencent.femas.jvm.monitor.jvmmonitoragent;

import com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent;
import com.tencent.femas.jvm.monitor.utils.*;
import org.jmxtrans.agent.JmxTransAgent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JvmMonitorAgentEntrance {

    private static final Logger LOGGER = Logger.getLogger(JvmMonitorAgentEntrance.class);
    private static final String DEFAULT_PORT = "11339";
    private static final String DEFAULT_JMXCFG_FILE = "tsf_monitor_config.xml";
    private static final String DEFAULT_BCI_ARGUMENTS = "";
    public static final String JDK_CTX = "/jvm";

    private static String configFile = ""; // = getDefaultJmxcfgFileFromJar(DEFAULT_JMXCFG_FILE);
    private static String portNum = DEFAULT_PORT;
    private static boolean enableDiagAgent = false;
    private static boolean enableMonitorAgent = true;
    private static String bciArguments = DEFAULT_BCI_ARGUMENTS;
    private static boolean hasController = false;
    private static ControllerType controllerType = ControllerType.HTTP;
    private static RpcServer controllerServer;
    private static volatile boolean agentLoaded = false;
    public static HashMap<String, Integer> countTable;

    enum ControllerType {
        SOCKET,
        HTTP
    }

    public static String getDefaultJmxcfgFileFromJar(String cfg, String pathToAdd) {
        String cfgFile;
        LOGGER.debug("try to load default jmx cfg file: " + DEFAULT_JMXCFG_FILE);
        try {
            if (pathToAdd == null || pathToAdd.length() == 0) {
                cfgFile = JvmMonitorUtils.getFileFromJar(cfg);
            } else {
                cfgFile = JvmMonitorUtils.getUpdatedFileFromJar(cfg, pathToAdd);

            }
        } catch (Exception e) {
            LOGGER.error("Fail load default performance monitoring config file:\n");
            e.printStackTrace();
            cfgFile = "";
        }
        return cfgFile;
    }

    private static void useDefaultOptions() {

        configFile = getDefaultJmxcfgFileFromJar(DEFAULT_JMXCFG_FILE, JvmMonitorUtils.getDataSavePath());
        LOGGER.debug("refreshed configFIle is " + configFile);
        portNum = DEFAULT_PORT;
        enableDiagAgent = false;
        bciArguments = DEFAULT_BCI_ARGUMENTS;
        hasController = false;
        controllerType = ControllerType.HTTP;
    }

    private static boolean parseBool(String string, String key) {
        if (string.equalsIgnoreCase("true")) {
            return true;
        } else if (!string.equalsIgnoreCase("false")) {
            LOGGER.warn("Invalid bool value for " + key + ", set to false");
        }
        return false;
    }



    private static boolean verifyArguments() {
        if (((portNum == null || portNum.length() == 0) && (hasController == true))
                || configFile == null || configFile.length() == 0) {
            LOGGER.error("invalid argument, disable Monitor and Diagnostic mode");
            enableMonitorAgent = false;
            enableDiagAgent = false;
            return false;
        }
        if (enableMonitorAgent == false && enableDiagAgent == false) {
            LOGGER.error("must enable Monitor or Diagnostic mode");
            return false;
        }
        return true;
    }

    private static void setDefaultArguments() {
        // by default hasController is false. if set to true.
        if (hasController) {
            if (controllerType == null) {
                controllerType = ControllerType.HTTP;
                LOGGER.warn("use default controller type: " + controllerType);
            }
            if (portNum == null || portNum.length() == 0) {
                if (controllerType == ControllerType.HTTP || controllerType == ControllerType.SOCKET) {
                    portNum = DEFAULT_PORT;
                    LOGGER.warn("use default controller port: " + portNum);
                }
            }
        } else {
            // No controller, monitor mode only.
            enableMonitorAgent = true;
            enableDiagAgent = false;
        }
    }

    private static void postProcessArguments() {
        finalizeJmxConfigFile();
        setDefaultArguments();
        verifyArguments();
    }

    private static void finalizeJmxConfigFile() {
        // user defined configFile.
        LOGGER.debug("finalize JMXConfigFile: " + configFile);
        if (configFile == null || configFile.length() == 0) {
            // only consider dataFile if it is TSFFileOutputWritter
            configFile = getDefaultJmxcfgFileFromJar(DEFAULT_JMXCFG_FILE, JvmMonitorUtils.getDataSavePath());
        }
        LOGGER.debug("Use config File at: " + configFile);

    }

    private static boolean fillOption(String kvPair) {
        String[] values = kvPair.split("=");
        if (values.length != 2) {
            LOGGER.error("Invalid Arguments pair: " + kvPair);
            return false;
        }
        String key = values[0];
        String val = values[1];
        LOGGER.debug("process options: " + key +  " = " + val);
        if (key.equalsIgnoreCase("jmxConf")) {
            configFile = val;
        } else if (key.equalsIgnoreCase("portNum")) {
            portNum = val;
        } else if (key.equalsIgnoreCase("enableDiagnosticAgent")) {
            enableDiagAgent = parseBool(val, key);
        } else if (key.equalsIgnoreCase("enableMonitorAgent")) {
            enableMonitorAgent = parseBool(val, key);
        } else if (key.equalsIgnoreCase("hasController")) {
            hasController = parseBool(val, key);
        } else if (key.equalsIgnoreCase("controllerType")) {
            if (val.equalsIgnoreCase("HTTP")) {
                controllerType = ControllerType.HTTP;
            } else if (val.equalsIgnoreCase(("SOCKET"))) {
                controllerType = ControllerType.SOCKET;
            } else {
                LOGGER.error("Invalid Arguments pair: " + kvPair);
                return false;
            }
        } else if (key.equalsIgnoreCase("dataSavePath")) {
            JvmMonitorUtils.setDataSavePath(val);
        } else {
            LOGGER.error("Invalid Arguments pair: " + kvPair);
            return false;
        }
        return true;
    }

    private static void processArguments(String options) {
        if (options == null || options.length() == 0) {
            useDefaultOptions();
            return;
        }
        String[] opts = options.split(";");
        try {
            for (String kvPair : opts) {
                // TODO. warning if fillOption returns false?
                fillOption(kvPair);
            }
            postProcessArguments();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static RpcServer createControllerServer(ControllerType type, Instrumentation inst) {
        if (type == ControllerType.SOCKET) {
            return RpcSocketServer.getInstance(portNum, inst);
        } else if (type == ControllerType.HTTP) {
            RpcServer instance =
                    HttpSocketServer.getInstance(portNum, new HotspotRequestHandler(inst));
            return instance;
        }
        LOGGER.error("Unsupported controller communication type: " + type.name());
        return null;
    }

    public static void profilerAgentMain(String options, Instrumentation inst) throws Exception {
        JvmMonitorBCIAgent.agentmain(options, inst);
    }

    public static void main(String options, Instrumentation inst) {
        if (agentLoaded) {
            LOGGER.warn("JvmMonitorAgent already loaded");
            return;
        }
        agentLoaded = true;
        LOGGER.debug("Start parsing options: " + options);
        processArguments(options);

        if (!JvmMonitorUtils.createDataPath()) {
            LOGGER.error("Fail create folder " + JvmMonitorUtils.getDataSavePath() + "disable JvmMonitorAgent");
            return;
        }

        // Monitoring has no relationship with hasController.
        if (enableMonitorAgent) {
            LOGGER.info("Start jmxtrans with config file " + configFile);
            JmxTransAgent.premain(configFile, inst);
        }

        if (hasController) {
            controllerServer = createControllerServer(controllerType, inst);
            if (controllerServer == null) {
                LOGGER.error("Fail create RPC Controller Server: " + controllerType.name()
                        + ". will work in monitor Only mode");
                enableDiagAgent = false;
            } else {
                LOGGER.info("Start Controller Server: " + controllerType.name());
                LOGGER.debug("Controller port: " + portNum);
            }
        }

        countTable = new HashMap<String, Integer>();

        ExecutorService exec = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        exec.execute(new Runnable() {
            @Override
            public void run() {
                if (controllerServer != null) {
                    try {
                        controllerServer.start(JDK_CTX);
                    } catch (IOException e) {
                        LOGGER.error("Error processing RPC Controller");
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
