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

package com.tencent.femas.tencentcloudjvmmonitor.jvmmonitoragent;

public class PerThreadDrawData {
    private String threadName;
    private Thread.State threadState;
    private boolean isNative;
    private String methodInfo;

    public String getTraceInfo() {
        return traceInfo;
    }

    public void setTraceInfo(String traceInfo) {
        this.traceInfo = traceInfo;
    }

    private String traceInfo;

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Thread.State getThreadState() {
        return threadState;
    }

    public void setThreadState(Thread.State threadState) {
        this.threadState = threadState;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        isNative = aNative;
    }

    public String getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(String methodInfo) {
        this.methodInfo = methodInfo;
    }


}
