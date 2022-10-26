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

import java.util.ArrayList;

public class ThreadInfoDrawData {
    private ThreadStateData threadStateData;
    private ArrayList<ThreadGroupData> threadGroupDataList;
    private int totalThreadGroupCount;
    private NativeStatData nativeThreadCountData;
    private int totalTraceGroupCount;
    private ArrayList<ThreadTraceGroupData> threadTraceGroupDataList;
    private int totalMethodDistCount;
    private ArrayList<MethodDistData> methodDistDataList;

    public ThreadInfoDrawData() {
        threadStateData = new ThreadStateData();
        threadGroupDataList = new ArrayList<>();
        nativeThreadCountData = new NativeStatData();
        threadTraceGroupDataList = new ArrayList<>();
        methodDistDataList = new ArrayList<>();

        totalThreadGroupCount = 0;
        totalTraceGroupCount = 0;
        totalMethodDistCount = 0;
    }

    public int getTotalTraceGroupCount() {
        return totalTraceGroupCount;
    }

    public void setTotalTraceGroupCount(int totalTraceGroupCount) {
        this.totalTraceGroupCount = totalTraceGroupCount;
    }

    public int getTotalMethodDistCount() {
        return totalMethodDistCount;
    }

    public void setTotalMethodDistCount(int totalMethodDistCount) {
        this.totalMethodDistCount = totalMethodDistCount;
    }

    public int getTotalThreadGroupCount() {
        return totalThreadGroupCount;
    }

    public void setTotalThreadGroupCount(int totalThreadGroupCount) {
        this.totalThreadGroupCount = totalThreadGroupCount;
    }

    public ThreadStateData getThreadStateData() {
        return threadStateData;
    }

    public void setThreadStateData(ThreadStateData threadStateData) {
        this.threadStateData = threadStateData;
    }

    public ArrayList<ThreadGroupData> getThreadGroupDataList() {
        return threadGroupDataList;
    }

    public void setThreadGroupDataList(ArrayList<ThreadGroupData> threadGroupDataList) {
        this.threadGroupDataList = threadGroupDataList;
    }

    public NativeStatData getNativeThreadCountData() {
        return nativeThreadCountData;
    }

    public void setNativeThreadCountData(NativeStatData nativeThreadCountData) {
        this.nativeThreadCountData = nativeThreadCountData;
    }

    public ArrayList<ThreadTraceGroupData> getThreadTraceGroupDataList() {
        return threadTraceGroupDataList;
    }

    public void setThreadTraceGroupDataList(ArrayList<ThreadTraceGroupData> threadTraceGroupDataList) {
        this.threadTraceGroupDataList = threadTraceGroupDataList;
    }

    public ArrayList<MethodDistData> getMethodDistDataList() {
        return methodDistDataList;
    }

    public void setMethodDistDataList(ArrayList<MethodDistData> methodDistDataList) {
        this.methodDistDataList = methodDistDataList;
    }

    /*
     *   update Data from perThreadDrawData.
     */
    public void update(PerThreadDrawData perThreadDrawData) {
        String threadName = perThreadDrawData.getThreadName();
        updateThreadGroupDataList(threadName);

        Thread.State state = perThreadDrawData.getThreadState();
        threadStateData.updateCount(state);

        String methodInfo = perThreadDrawData.getMethodInfo();
        updateMethodDistData(methodInfo);
        
        String traceInfo = perThreadDrawData.getTraceInfo();
        updateThreadTraceGroupDataList(traceInfo);

        if (perThreadDrawData.isNative()) {
            nativeThreadCountData.setNonJavaCount(nativeThreadCountData.getNonJavaCount() + 1);
        } else {
            nativeThreadCountData.setJavaThreadCount(nativeThreadCountData.getJavaThreadCount() + 1);
        }
    }

    private void updateMethodDistData(String methodInfo) {
        for (MethodDistData entry : methodDistDataList) {
            if (methodInfo.equals(entry.getMethodName())) {
                entry.setCount(entry.getCount() + 1);
                return;
            }
        }

        MethodDistData data = new MethodDistData();
        data.setMethodName(methodInfo);
        data.setCount(1);
        data.setMethodId("method_" + totalMethodDistCount);
        totalMethodDistCount++;
        methodDistDataList.add(data);
    }

    private void updateThreadTraceGroupDataList(String methodInfo) {
        for (ThreadTraceGroupData entry : threadTraceGroupDataList) {
            if (methodInfo.equals(entry.getTraceString())) {
                entry.setCount(entry.getCount() + 1);
                return;
            }
        }

        ThreadTraceGroupData data = new ThreadTraceGroupData();
        data.setTraceString(methodInfo);
        data.setCount(1);
        data.setTraceId("stackTrace_" + totalTraceGroupCount);
        totalTraceGroupCount++;
        threadTraceGroupDataList.add(data);
    }

    private void updateThreadGroupDataList(String threadName) {
        String name = threadName.replaceAll("\\d*$", "");
        for (ThreadGroupData entry : threadGroupDataList) {
            if (name.equals(entry.getGroupName())) {
                entry.setCount(entry.getCount() + 1);
                return;
            }
        }
        ThreadGroupData data = new ThreadGroupData();
        data.setGroupName(name);
        data.setCount(1);
        threadGroupDataList.add(data);
        totalThreadGroupCount++;
    }
}
