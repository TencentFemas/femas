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

import com.tencent.femas.jvm.monitor.jvmmonitoragent.JvmMonitorAgentEntrance;
import com.tencent.femas.jvm.monitor.utils.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class JvmMonitorBCIAgentOptions {

    // action and args:  \
    //      "trace" {times, args} // method invocation trace
    //      "singleStep" {time, args}
    //      "profile" // method argument, return values.
    //      "Details" trace + profile.
    //      "Reset"  -- when set, reset all class to original one, ignore all other arguments.
    private static final String ACTION_DETAIL = "Detail";
    private static final String ACTION_TRACE = "Trace";
    private static final String ACTION_PROFILE = "Profile";
    private static final String ACTION_SINGLE_STEP = "SingleStep";
    private static final String ACTION_RESET = "Reset";

    public static final int MODE_NULL = 0;
    public static final int MODE_DETAIL = 1;
    public static final int MODE_TRACE = 1 << 1;
    public static final int MODE_PROFILE = 1 << 2;
    public static final int MODE_SINGLE_STEP = 1 << 3;
    public static final int MODE_RESET = 1 << 4;

    private int mode;
    private int profileCount;
    private ArrayList<TraceMethodInfo> traceInfoList;
    private ArrayList<TraceMethodInfo> restoreInfoList;


    private static final Logger LOGGER = Logger.getLogger(JvmMonitorBCIAgentOptions.class);

    // action=detail,count=1, class.method,class.*,class*, class
    // for restore !class.method
    public JvmMonitorBCIAgentOptions() {
        mode = MODE_NULL;
        profileCount = 0;
        traceInfoList = new ArrayList<TraceMethodInfo>();
        restoreInfoList = new ArrayList<TraceMethodInfo>();
    }

    // String options = "action=" + action + ",count=" + meta.getProfileCount() + "," + meta.getArgs();
    // action and args:  \
    //      "trace" {times, args} // method invocation trace
    //      "singleStep" {time, args}
    //      "profile" // method argument, return values.
    //      "Details" trace + profile.
    //      "reset"
    public void parseOptions(String options) throws Exception {
        if (options == null || options.length() == 0) {
            throw new Exception("No methods to trace");
        }

        String[] opts = options.split(",");
        // m could be any regular expression
        for (String m : opts) {
        //    LOGGER.debug("start parsing m = " + m);
            m = m.trim();
            if (m.length() == 0) {
                throw new Exception("Empty value for tracing");
            }
            boolean restore = false;
            if (m.startsWith("action=")) {

                String action = m.substring("action=".length());
                LOGGER.debug(" parse action= " + action);
                if (!setAction(action)) {
                    throw new Exception("Unknown profile action: " + action);
                }
                continue;
            }
            if (m.startsWith("count=")) {
                String c = m.substring("count=".length());
                LOGGER.debug(" parse count= " + c);
                try {
                    profileCount = Integer.parseInt(c);
                } catch (NumberFormatException e) {
                    throw new Exception("Illegal number format for count: " + c);
                }
                continue;
            }

            // for other arguments , recognize them as method name.
            String kpart = "";
            String mpart = "";

            if (profileCount <= 0) {
                restore = true;
            }
            // process all arguments.
            {
                int idx = m.lastIndexOf(".");
                if (idx == -1) {
                    kpart = m;
                    mpart = "*";
                } else {
                    kpart = m.substring(0, idx);
                    //  for kpart, change . to \\.
                    kpart.replace(".", "\\\\.");
                    mpart = m.substring(idx + 1);
                    if (mpart == null || mpart.length() < 1) {
                        throw new Exception("Illegal method for tracing: " + m);
                    }
                }
            }
            addTraceInfo(kpart, mpart, restore);

            String newKey = kpart + "." + mpart;
            // restore all the other traceinfo
            if (JvmMonitorAgentEntrance.countTable.size() > 0) {
                Iterator iter = JvmMonitorAgentEntrance.countTable.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    if (!key.equals(newKey)) {
                        int idx = key.lastIndexOf(".");
                        JvmMonitorAgentEntrance.countTable.remove(key);
                        String keyPart = key.substring(0, idx);
                        String methodPart = key.substring(idx + 1);
                        addTraceInfo(keyPart, methodPart, true);
                    }
                }
            }

            JvmMonitorAgentEntrance.countTable.put(newKey, profileCount);
        }
    }

    private boolean setAction(String action) {
        boolean ret = false;
        if (action.equalsIgnoreCase(ACTION_DETAIL)) {
            mode |= MODE_DETAIL;
            ret = true;
        } else if (action.equalsIgnoreCase(ACTION_TRACE)) {
            mode |= MODE_TRACE;
            ret = true;
        } else if (action.equalsIgnoreCase(ACTION_PROFILE)) {
            mode |= MODE_PROFILE;
            ret = true;
        } else if (action.equalsIgnoreCase(ACTION_RESET)) {
            mode |= MODE_RESET;
            ret = true;
        } else if (action.equalsIgnoreCase(ACTION_SINGLE_STEP)) {
            LOGGER.error("Single Step mode is not implemented");
            mode |= MODE_SINGLE_STEP;
            ret = false;
        } else {
            LOGGER.error("Unknown method profile mode");
        }
        return ret;
    }

    private void addTraceInfo(String k, String m, boolean restore) {
        if (restore) {
            LOGGER.debug("add Restore TraceInfo: klass: " + k  + " method: "  + m);
            restoreInfoList.add(new TraceMethodInfo(k,m));
        } else {
            LOGGER.debug("add trace TraceInfo: klass: " + k + " Method: " + m);
            traceInfoList.add(new TraceMethodInfo(k, m));
        }
    }

    public ArrayList<TraceMethodInfo> getTraceInfoList() {
        return traceInfoList;
    }

    public ArrayList<TraceMethodInfo> getRestoreInfoList() {
        return restoreInfoList;
    }

    public int getMode() {
        return mode;
    }

    public int getProfileCount() {
        return profileCount;
    }

    public boolean isInResetMode() {
        return (getMode() & JvmMonitorBCIAgentOptions.MODE_RESET) != 0;
    }


    public void clearTraceInfoList() {
        traceInfoList.clear();
        restoreInfoList.clear();
    }

    public static class TraceMethodInfo {
        String klassName;
        String methodName;

        public TraceMethodInfo(String klassName, String methodName) {
            this.klassName = klassName;
            this.methodName = methodName;
        }
    }

}
