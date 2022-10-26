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

package com.tencent.femas.jvm.monitor.bci;

import com.tencent.femas.jvm.monitor.utils.Logger;
import com.tencent.tsf.femas.agent.classloader.InterceptorClassLoaderCache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// wrapper class used to load recorder with qocoLoader
public class JvmMonitorMethodTraceRecorderWrapper {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorMethodTraceRecorderWrapper.class);

    private static ClassLoader qLoader;
    private static final String METHOD_TRACE_RECORDER_CLASS =
            "com.tencent.qcloudmiddleware.tencentcloudjvmMonitor.bci.JvmMonitorMethodTraceRecorder";

    private static Class<?> recorderClass;

    static {
        qLoader = InterceptorClassLoaderCache.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            recorderClass = Class.forName(METHOD_TRACE_RECORDER_CLASS, true, qLoader);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Can not successfully load class: " + METHOD_TRACE_RECORDER_CLASS);
            e.printStackTrace();
        }
    }

//    static {
//        qLoader = TencentJvmMonitorAgent.getQocoLoader();
//        try {
//            LOGGER.debug("start to load trace recorder method, loader is: " + qLoader
//                    + " self loader is " + JvmMonitorMethodTraceRecorderWrapper.class.getClassLoader()
//                    + "agentLoader is " + TencentJvmMonitorAgent.class.getClassLoader());
//            recorderClass = Class.forName(METHOD_TRACE_RECORDER_CLASS, true, qLoader);
//        } catch (ClassNotFoundException e) {
//            LOGGER.error("Can not successfully load class: " + METHOD_TRACE_RECORDER_CLASS);
//            e.printStackTrace();
//        }
//    }

    public static void onMethodEnter(String className, String methodSig, String loaderSig) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onMethodEnter", String.class, String.class, String.class);
            m.invoke(null, className, methodSig, loaderSig);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static void onMethodExit(String className, String methodSig, String loaderSig, boolean returnNormally) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onMethodExit", String.class, String.class, String.class,
                    boolean.class);
            m.invoke(null, className, methodSig, loaderSig, returnNormally);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public static void beforeMethodCall(String owner, String name, String descriptor, int line) {
        if (recorderClass == null) {
            return;
        }
        try {
            //LOGGER.debug("beforeMethodCall - " + owner + "." + name + descriptor + "@" + line);
            Method m = recorderClass.getMethod("beforeMethodCall", String.class, String.class, String.class,
                    int.class);
            m.invoke(null, owner, name, descriptor, line);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void afterMethodCall(String owner, String name, String descriptor, int line) {
        if (recorderClass == null) {
            return;
        }
        try {
            // LOGGER.debug("afterMethodCall - " + owner + "." + name + descriptor + "@" + line);
            Method m = recorderClass.getMethod("afterMethodCall", String.class, String.class, String.class,
                    int.class);
            m.invoke(null, owner, name, descriptor, line);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushBool(int idx, boolean arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushBool", int.class, boolean.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushChar(int idx, char arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushChar", int.class, char.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushByte(int idx, byte arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushByte", int.class, byte.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushShort(int idx, short arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushShort", int.class, short.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushInt(int idx, int arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushInt", int.class, int.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushFloat(int idx, float arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushFloat", int.class, float.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushDouble(int idx, double arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushDouble", int.class, double.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushLong(int idx, long arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushLong", int.class, long.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentPushObject(int idx, Object arg, boolean isArgument) {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentPushObject", int.class, Object.class, boolean.class);
            m.invoke(null, idx, arg, isArgument);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void onArgumentsPushFinishAll() {
        if (recorderClass == null) {
            return;
        }
        try {
            Method m = recorderClass.getMethod("onArgumentsPushFinishAll");
            m.invoke(null);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
