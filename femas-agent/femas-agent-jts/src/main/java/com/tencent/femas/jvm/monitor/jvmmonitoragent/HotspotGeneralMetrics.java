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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tencent.femas.jvm.monitor.utils.Logger;

import java.lang.management.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HotspotGeneralMetrics {
    private static final Logger LOGGER = Logger.getLogger(HotspotGeneralMetrics.class);
    private static final int KB_shift = 10;
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private static CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
    private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();

    private int elapsedTime;
    private int cpuConsumingTime;
    private int jitCompileTime;
    private int heapUsed;
    private int heapFree;
    private int heapSize;
    private int heapCommitted;
    private int nonHeapUsed;
    private int nonHeapSize;
    private int nonHeapCommitted;
    private int survivorSpaceUsed;
    private int survivorSpaceSize;
    private int survivorSpaceCommitted;
    private int edenSpaceUsed;
    private int edenSpaceSize;
    private int edenSpaceCommitted;
    private int oldSpaceSize;
    private int oldSpaceUsed;
    private int oldSpaceCommitted;
    private int metaSpaceUsed;
    private int metaSpaceSize;
    private int metaSpaceCommitted;
    private int objectPendingFinalizationCount;
    private int youngGcCount;
    private int fullGcCount;
    private int youngGcTimeTotal;
    private int fullGcTimeTotal;
    private int activeThreadCount;
    private int daemonThreadCount;
    private int totalThreadCount;
    private int cpuUtil;
    private int classCount;

    @JsonIgnore
    private long longYGcTimeTotal;
    @JsonIgnore
    private long longFGcTimeTotal;

    static final Set<String> YOUNG_GC = new HashSet<String>(3);
    static final Set<String> OLD_GC = new HashSet<String>(3);

    static {
        // young generation GC names
        YOUNG_GC.add("PS Scavenge");
        YOUNG_GC.add("ParNew");
        YOUNG_GC.add("G1 Young Generation");
        YOUNG_GC.add("Copy");

        // old generation GC names
        OLD_GC.add("PS MarkSweep");
        OLD_GC.add("ConcurrentMarkSweep");
        OLD_GC.add("G1 Old Generation");
        OLD_GC.add("MarkSweepCompact");
    }


    public void collect() {

        elapsedTime = (int) (runtimeMXBean.getUptime());
        com.sun.management.ThreadMXBean tmxBean = (com.sun.management.ThreadMXBean)threadMXBean;
        long totalCpuConsumingTime = 0L;
        if (false && threadMXBean.isCurrentThreadCpuTimeSupported()) {
            LOGGER.info("current thread cpu time suported! " + tmxBean.getCurrentThreadCpuTime());
            cpuConsumingTime = (int)(tmxBean.getCurrentThreadCpuTime() / 1000000);
        } else {
            long[] ids = threadMXBean.getAllThreadIds();
            long[] ct = tmxBean.getThreadCpuTime(ids);
            for (int i = 0; i < ct.length; i++) {
                long t = ct[i];
                if (t < 0) {
                    continue;
                }
                totalCpuConsumingTime += t;
            }
            cpuConsumingTime = (int) (totalCpuConsumingTime / 1000000);
        }

        List<MemoryPoolMXBean> memoryPoolMXBeans
                = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String name = memoryPoolMXBean.getName();
            if (name.contains("Eden Space")) {
                edenSpaceSize = (int) (memoryPoolMXBean.getUsage().getMax() >> KB_shift);
                edenSpaceCommitted = (int) (memoryPoolMXBean.getUsage().getCommitted() >> KB_shift);
                edenSpaceUsed = (int) (memoryPoolMXBean.getUsage().getUsed() >> KB_shift);
            } else if (name.contains("Survivor Space")) {
                survivorSpaceSize = (int) (memoryPoolMXBean.getUsage().getMax() >> KB_shift);
                survivorSpaceCommitted = (int) (memoryPoolMXBean.getUsage().getCommitted() >> KB_shift);
                survivorSpaceUsed = (int) (memoryPoolMXBean.getUsage().getUsed() >> KB_shift);
            } else if (name.contains("Old Gen")) {
                oldSpaceSize = (int) (memoryPoolMXBean.getUsage().getMax() >> KB_shift);
                oldSpaceCommitted = (int) (memoryPoolMXBean.getUsage().getCommitted() >> KB_shift);
                oldSpaceUsed = (int) (memoryPoolMXBean.getUsage().getUsed() >> KB_shift);
            } else if (name.contains("Metaspace")) {
                metaSpaceSize = (int) (memoryPoolMXBean.getUsage().getMax() >> KB_shift);
                metaSpaceCommitted = (int) (memoryPoolMXBean.getUsage().getCommitted() >> KB_shift);
                metaSpaceUsed = (int) (memoryPoolMXBean.getUsage().getUsed() >> KB_shift);
            }
        }
        jitCompileTime = (int) compilationMXBean.getTotalCompilationTime();

        MemoryUsage mu = memoryMXBean.getHeapMemoryUsage();
        heapSize = (int) (mu.getMax() >> KB_shift);
        heapUsed = (int) (mu.getUsed() >> KB_shift);
        heapFree = heapSize - heapUsed;
        heapCommitted = (int) (mu.getCommitted() >> KB_shift);

        mu = memoryMXBean.getNonHeapMemoryUsage();
        nonHeapCommitted = (int) (mu.getCommitted() >> KB_shift);
        nonHeapSize = (int) (mu.getMax() >> KB_shift);
        nonHeapUsed = (int) (mu.getUsed() >> KB_shift);

        objectPendingFinalizationCount = memoryMXBean.getObjectPendingFinalizationCount();

        List<GarbageCollectorMXBean> mxBeans
                = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : mxBeans) {
            long count = gc.getCollectionCount();
            if (count >= 0) {
                if (YOUNG_GC.contains(gc.getName())) {
                     youngGcCount = (int)count;
                     longYGcTimeTotal = gc.getCollectionTime();
                     youngGcTimeTotal = (int)(longYGcTimeTotal);
                } else if (OLD_GC.contains(gc.getName())) {
                    fullGcCount = (int)count;
                    longFGcTimeTotal = gc.getCollectionTime();
                    fullGcTimeTotal = (int)(longFGcTimeTotal);
                } else {
                    fullGcCount = (int)count;
                    longFGcTimeTotal = gc.getCollectionTime();
                    fullGcTimeTotal = (int)(longFGcTimeTotal);
                }
            }
        }

        activeThreadCount = threadMXBean.getThreadCount();
        daemonThreadCount = threadMXBean.getDaemonThreadCount();
        totalThreadCount = (int) threadMXBean.getTotalStartedThreadCount();
        cpuUtil = calCpuLoad();
        classCount = classLoadingMXBean.getLoadedClassCount();
    }

    private int calCpuLoad() {
        com.sun.management.OperatingSystemMXBean omx = (com.sun.management.OperatingSystemMXBean)operatingSystemMXBean;
        double load = 0.0;
        for (int i = 0; i < 10; i++) {
            load = omx.getSystemCpuLoad();
            if (load >= 0.0 && load <= 1.0) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("Problem when calculate CPU load");
                break;
            }
        }
        return (int)(load * 100.0);
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getCpuConsumingTime() {
        return cpuConsumingTime;
    }

    public void setCpuConsumingTime(int cpuConsumingTime) {
        this.cpuConsumingTime = cpuConsumingTime;
    }

    public int getJitCompileTime() {
        return jitCompileTime;
    }

    public void setJitCompileTime(int jitCompileTime) {
        this.jitCompileTime = jitCompileTime;
    }

    public int getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(int heapUsed) {
        this.heapUsed = heapUsed;
    }

    public int getHeapFree() {
        return heapFree;
    }

    public void setHeapFree(int heapFree) {
        this.heapFree = heapFree;
    }

    public int getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(int heapSize) {
        this.heapSize = heapSize;
    }

    public int getHeapCommitted() {
        return heapCommitted;
    }

    public void setHeapCommitted(int heapCommitted) {
        this.heapCommitted = heapCommitted;
    }

    public int getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public void setObjectPendingFinalizationCount(int objectPendingFinalizationCount) {
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
    }

    public int getYoungGcCount() {
        return youngGcCount;
    }

    public void setYoungGcCount(int youngGcCount) {
        this.youngGcCount = youngGcCount;
    }

    public int getFullGcCount() {
        return fullGcCount;
    }

    public void setFullGcCount(int fullGcCount) {
        this.fullGcCount = fullGcCount;
    }

    public int getYoungGcTimeTotal() {
        return youngGcTimeTotal;
    }

    public void setYoungGcTimeTotal(int youngGcTimeTotal) {
        this.youngGcTimeTotal = youngGcTimeTotal;
    }

    public int getFullGcTimeTotal() {
        return fullGcTimeTotal;
    }

    public void setFullGcTimeTotal(int fullGcTimeTotal) {
        this.fullGcTimeTotal = fullGcTimeTotal;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    public void setActiveThreadCount(int activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    public int getClassCount() {
        return classCount;
    }

    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }

    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public void setTotalThreadCount(int totalThreadCount) {
        this.totalThreadCount = totalThreadCount;
    }

    public int getTotalThreadCount() {
        return totalThreadCount;
    }

    public int getCpuUtil() {
        return cpuUtil;
    }

    public void setCpuUtil(int cpuUtil) {
        this.cpuUtil = cpuUtil;
    }

    public int getEdenSpaceCommitted() {
        return edenSpaceCommitted;
    }

    public void setEdenSpaceCommitted(int edenSpaceCommitted) {
        this.edenSpaceCommitted = edenSpaceCommitted;
    }

    public int getEdenSpaceSize() {
        return edenSpaceSize;
    }

    public void setEdenSpaceSize(int edenSpaceSize) {
        this.edenSpaceSize = edenSpaceSize;
    }

    public int getEdenSpaceUsed() {
        return edenSpaceUsed;
    }

    public void setEdenSpaceUsed(int edenSpaceUsed) {
        this.edenSpaceUsed = edenSpaceUsed;
    }

    public int getSurvivorSpaceCommitted() {
        return survivorSpaceCommitted;
    }

    public void setSurvivorSpaceCommitted(int survivorSpaceCommitted) {
        this.survivorSpaceCommitted = survivorSpaceCommitted;
    }

    public int getSurvivorSpaceSize() {
        return survivorSpaceSize;
    }

    public void setSurvivorSpaceSize(int survivorSpaceSize) {
        this.survivorSpaceSize = survivorSpaceSize;
    }

    public int getSurvivorSpaceUsed() {
        return survivorSpaceUsed;
    }

    public void setSurvivorSpaceUsed(int survivorSpaceUsed) {
        this.survivorSpaceUsed = survivorSpaceUsed;
    }

    public void setOldSpaceCommitted(int oldSpaceCommitted) {
        this.oldSpaceCommitted = oldSpaceCommitted;
    }

    public int getOldSpaceCommitted() {
        return oldSpaceCommitted;
    }

    public void setOldSpaceSize(int oldSpaceSize) {
        this.oldSpaceSize = oldSpaceSize;
    }

    public int getOldSpaceSize() {
        return oldSpaceSize;
    }

    public void setOldSpaceUsed(int oldSpaceUsed) {
        this.oldSpaceUsed = oldSpaceUsed;
    }

    public int getOldSpaceUsed() {
        return oldSpaceUsed;
    }

    public void setMetaSpaceCommitted(int metaSpaceCommitted) {
        this.metaSpaceCommitted = metaSpaceCommitted;
    }

    public int getMetaSpaceCommitted() {
        return metaSpaceCommitted;
    }

    public void setMetaSpaceSize(int metaSpaceSize) {
        this.metaSpaceSize = metaSpaceSize;
    }

    public int getMetaSpaceSize() {
        return metaSpaceSize;
    }

    public void setMetaSpaceUsed(int metaSpaceUsed) {
        this.metaSpaceUsed = metaSpaceUsed;
    }

    public int getMetaSpaceUsed() {
        return metaSpaceUsed;
    }

    public void setNonHeapCommitted(int nonHeapCommitted) {
        this.nonHeapCommitted = nonHeapCommitted;
    }

    public int getNonHeapCommitted() {
        return nonHeapCommitted;
    }

    public void setNonHeapSize(int nonHeapSize) {
        this.nonHeapSize = nonHeapSize;
    }

    public int getNonHeapSize() {
        return nonHeapSize;
    }

    public void setNonHeapUsed(int nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }

    public int getNonHeapUsed() {
        return nonHeapUsed;
    }
}
