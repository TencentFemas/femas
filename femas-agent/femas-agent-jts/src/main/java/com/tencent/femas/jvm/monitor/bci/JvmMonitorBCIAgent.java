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

package com.tencent.femas.jvm.monitor.bci;

import com.tencent.femas.jvm.monitor.utils.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Pattern;

// performance tracing tier 1-3
// 1. performance in profiling mode is not important.
// 2. performance in profiling mode is important but user expect to get more info with performance impact at 10%
// 3. performance in profiling mode is import, and user only want to collect what they want.
// performance impact less than 5%.
// different scenario may use different configuration for profiling....
// TODO: more performance data and logic for configuration, limitation and others.

public class JvmMonitorBCIAgent {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorBCIAgent.class);
    private static JvmMonitorTransformer transformer;
    private static JvmMonitorRetrieveTransformer retrieveTransformer;
    private static int version;
    private static Instrumentation instrumentation;

    static {
        LOGGER.debug("------ initialize transformer!!!");
        transformer = new JvmMonitorTransformer();
        retrieveTransformer = new JvmMonitorRetrieveTransformer();
        version = 0;
        instrumentation = null;
    }

    public static void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    public static Instrumentation getInstrumentation() {

        return instrumentation;
    }

    public static JvmMonitorTransformer getJvmMonitorTransformer() {
        return transformer;
    }

    public static void premain(String options, Instrumentation inst) {
        // start http server, wait for request.
        // options, should contain http port.
        // TODO, merge to JVM MonitorAgentEntrance.
        LOGGER.info("BCI agent loaded");
    }

    // used for attach
    // options: action=Detail, count=1,class.method,class.*,class*, class
    public static void agentmain(String options, Instrumentation inst) throws Exception {
        setInstrumentation(inst);

        // start http, get request and transform.
        com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions bciOptions = new com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions();

            // lock on the class, so there can only be one instrumentation at a time
            synchronized (JvmMonitorBCIAgent.class) {
                bciOptions.parseOptions(options);
                // add transformer.

                doInstrumentation(inst, bciOptions);
                bciOptions.clearTraceInfoList();
            }

    }

    public static String getPatternFromString(String str) {
        return str.replace("*", ".*");
    }

    /*
        1. Get all loaded classes
        2. if TraceInfoList contains empty class, retrieve and return
        3. if No matched classes , retrieve and return
        4. if matched classes, to transformation and return
     */
    private static void doInstrumentation(Instrumentation inst, com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions bciOptions) {
        boolean resetAll = bciOptions.isInResetMode();
        // if in reset all mode , clear all registered infos and clear all MethodInfos.
        if (resetAll) {
            LOGGER.debug("ResetALL!!!!! :");
            // move all registered classes to reset list.
            transformer.resetAllRegisteredClasses();
        } else {
            // found classes that needs to be enhanced.
            Class<?>[] loadedClasses = inst.getAllLoadedClasses();
            // already modified saved in transformer.registerclasses.
            // new candidate in getTraceInfoList()
            ArrayList<com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo> newTraceInfo = bciOptions.getTraceInfoList();

            LOGGER.debug("trace info from argument: " + newTraceInfo + " count: " + newTraceInfo.size()
                    + " registered classes count " + transformer.getRegisteredClasses().size());

            // restore list.
            ArrayList<com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo> allRestoreInfo = bciOptions.getRestoreInfoList();
            LOGGER.debug("restore info from argument: " + allRestoreInfo + " count: " + allRestoreInfo.size());

            // the logic first find all possibly classes need to be modified. a
            // nd then filter-out those that match in resetList;
            for (com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo info : newTraceInfo) {
                String clzPattern = getPatternFromString(info.klassName);
                String methodPattern = getPatternFromString(info.methodName);
                LOGGER.debug("start to find class that match pattern { " + clzPattern + " } + method + { "
                        + methodPattern + " }");
                for (Class clz : loadedClasses) {
                    // name used in forName()
                    String cname = clz.getName();

                    if (shouldExcludeClass(cname)) {
                        // LOGGER.debug("Exclude class {" + cname + "} for instrument");
                        continue;
                    }
                    boolean found = false;
                    if (Pattern.matches(clzPattern, cname)) {
                        Method[] clzMethods = clz.getDeclaredMethods();
                        // boolean found = false;
                        for (Method mtd : clzMethods) {
                            if (Pattern.matches(methodPattern, mtd.getName())) {
                                LOGGER.debug("register enhanced method {" + clz.getName() + "." + mtd.getName()
                                        + "} that match pattern: {" + methodPattern + "} ");
                                transformer.registerClassAndMethod(cname, clz, mtd.getName());
                                found = true;
                            }
                        }
                        if (found == false) {
                            //LOGGER.debug("WARNING: Can not find method in class (" + cname +
                            // ") that match method pattern: " + methodPattern);
                            continue;
                        }
                    }
                }
            }

            LOGGER.debug("ZLIN -Start processing restore info:  count: " + allRestoreInfo.size());
            // now we have all possible classes-need-trace.  filter-out those in restore list
            for (com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo info : allRestoreInfo) {
                String clzPattern = getPatternFromString(info.klassName);
                String methodPattern = getPatternFromString(info.methodName);
                LOGGER.debug("start to find restore class that match pattern { " + clzPattern + " }");
                for (Class clz : transformer.getRegisteredClasses()) {
                    // name used in forName()
                    String cname = clz.getName();
                    // this is not possible. delete me
                    if (shouldExcludeClass(cname)) {
                        // LOGGER.debug("Exclude class {" + cname + "} for instrument");
                        continue;
                    }
                    if (Pattern.matches(clzPattern, cname)) {
                        Method[] clzMethods = clz.getDeclaredMethods();
                        // boolean found = false;
                        for (Method mtd : clzMethods) {
                            if (Pattern.matches(clzPattern, cname) && Pattern.matches(methodPattern, mtd.getName())) {
                                LOGGER.debug("filter out method {" + clz.getName() + "." + mtd.getName()
                                        + "} that in restore list");
                                transformer.removeRegisteredMethod(cname, clz, mtd.getName());
                            }
                        }
                    }
                }
            }
        }
        try {

              TreeSet<Class<?>> classesSet = transformer.getRegisteredClasses();
              if (classesSet.size() == 0) {
                  if (resetAll) {
                      LOGGER.warn("No class to reset at present");
                  } else {
                      LOGGER.warn("Nothing to trace");
                  }
              } else {
                  inst.addTransformer(transformer, true);
                  Class<?>[] classesArray = new Class<?>[classesSet.size()];
                  classesArray = classesSet.toArray(classesArray);
                  LOGGER.debug("start to retransform classes count: " + classesArray.length);
                  inst.retransformClasses(classesArray);
                  transformer.postTransformProcess();
                  inst.removeTransformer(transformer);
              }
            } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
    }

    private static boolean shouldExcludeMethod(String cname, String mname,
                                               ArrayList<com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo> allRestoreInfo) {
        for (com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions.TraceMethodInfo info : allRestoreInfo) {
            String clzPattern = getPatternFromString(info.klassName);
            String methodPattern = getPatternFromString(info.methodName);
            if (Pattern.matches(clzPattern, cname) && Pattern.matches(methodPattern,mname)) {
                return true;
            }
        }
        return false;
    }


    public static boolean shouldExcludeClass(String cname) {
        return cname.startsWith("java.lang") || cname.startsWith("java.util") || cname.startsWith("java.security")
                || cname.startsWith("com.tencent.qcloudmiddleware.tencentcloudjvmmonitor")
                || cname.startsWith("java.io.PrintStream");
    }

}
