/**
 * Copyright 2010-2021 the original author or authors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.jvm.monitor.cmdline;

import com.tencent.femas.jvm.monitor.utils.JvmMonitorUtils;
import com.tencent.femas.jvm.monitor.utils.Logger;
//import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// attach=<target-ip:port | pid> listen=<ip:port> mode=<all | debug | monitor> monitor_config=<xxx.xml>
// Target:  JVM agent
// Listen:  ip:port communicate with TSF Agent
// mode:  debug means BCI online diagnostic; monitor is jmxtrans based monitoring.
// monitor_config: location of jmx-trans config files.
public class JvmMonitorArgs {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorArgs.class);
    public static final int WORK_MODE_MONITOR = 0x1;
    public static final int WORK_MODE_DEBUG = 0x10;
    public static final int WORK_MODE_ALL = WORK_MODE_DEBUG & WORK_MODE_MONITOR;
    private static volatile JvmMonitorArgs instance;
    private static final String CMDLINE_CONFIG_FILE = "cmdline_config.properties";
    public static final String DEFAULT_HTTP_FOLDER = "/QocoController";
    private static final String PING_INTERVAL_SECONDS = "60";

    //    private ConnectMode connectMode;
    private int workMode;
    private String pingHotspotInterval;
    // hotspot process info.
    // private String hotspotPid;
    private String hotspotIp;
    private String hotspotPort;
    // self port as http server.
    private String httpServerPort;


    public static JvmMonitorArgs getInstance() {
        if (instance == null) {
            synchronized (JvmMonitorArgs.class) {
                if (instance == null) {
                    instance = new JvmMonitorArgs();
                }
            }
        }
        return instance;
    }

    public String getHttpServerPort() {
        return httpServerPort;
    }

    public String getPingHotspotInterval() {
        return pingHotspotInterval;
    }

    public void setPingHotspotInterval(String pingHotspotInterval) {
        this.pingHotspotInterval = pingHotspotInterval;
    }

    private JvmMonitorArgs() {
        this.pingHotspotInterval = PING_INTERVAL_SECONDS;
        parseConfigFile();
    }

    private void loadArgumentsFromProperties(Properties prop) {

        this.hotspotIp = prop.getProperty("hotspot-ip", this.hotspotIp);
        this.hotspotPort = prop.getProperty("hotspot-port", this.hotspotPort);
        this.httpServerPort = prop.getProperty("http-server-port", this.httpServerPort);
        this.pingHotspotInterval = prop.getProperty("pingHotspotInterval", this.pingHotspotInterval);
        String dataSavePath = prop.getProperty("dataSavePath");
        if (dataSavePath != null) {
            JvmMonitorUtils.setDataSavePath(dataSavePath);
        }

        String mode = prop.getProperty("mode", "monitor");
        if (mode.equalsIgnoreCase("all")) {
            workMode = WORK_MODE_ALL;
        } else if (mode.equalsIgnoreCase("monitor")) {
            workMode = WORK_MODE_MONITOR;
        } else if (mode.equalsIgnoreCase("debug")) {
            workMode = WORK_MODE_DEBUG;
        }
    }


    // Load properties from QOCO_CONFIG_FILE
    private void parseConfigFile() {
        Properties prop = new Properties();
        LOGGER.debug("read command line args from: " + CMDLINE_CONFIG_FILE);
        InputStream ips = JvmMonitorArgs.class.getClassLoader().getResourceAsStream(CMDLINE_CONFIG_FILE);
        try {
            prop.load(ips);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadArgumentsFromProperties(prop);
    }

    public JvmMonitorArgs parse(String[] args) {
        int i;
        if ((args.length == 0) || ((args.length == 1) && args[0].equals("help"))) {
            printHelp();
        }
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!processArguments(args)) {
                LOGGER.error("Invalid Argument: " + arg);
                printHelp();
                System.exit(1);
            }
        }
        return this;
    }


    private boolean processArguments(String[] args) {
        Properties prop = new Properties();
        for (String arg : args) {
            LOGGER.debug("Process Arguments from Commandline: " + arg);
            String[] kv = arg.split("=");
            prop.put(kv[0], kv[1]);
        }

        loadArgumentsFromProperties(prop);
        return checkArguments();
    }

    private boolean checkArguments() {
        if (hotspotPort == null || hotspotIp == null
                || hotspotPort.length() == 0 || hotspotIp.length() == 0) {
            return false;
        }
        return true;
//        return verifyIpAddr(hotspotIp);
    }

//    private boolean verifyIpAddr(String ipAddr) {
//        if (ipAddr.equalsIgnoreCase("localhost")) {
//            return true;
//        }
//        return InetAddressValidator.getInstance().isValid(ipAddr);
//    }

    private void printHelp() {

    }

    public String getHotspotIp() {
        return hotspotIp;
    }

    public String getHotspotPort() {
        return hotspotPort;
    }

    public boolean doMonitor() {
        return (workMode & WORK_MODE_MONITOR) != 0;
    }

    public boolean doDebug() {
        return (workMode & WORK_MODE_DEBUG) != 0;
    }

}
