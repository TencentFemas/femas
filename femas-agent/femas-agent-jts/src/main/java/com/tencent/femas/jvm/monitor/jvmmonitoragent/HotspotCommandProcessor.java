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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.ThreadMXBean;
import com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent;
import com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgentOptions;
import com.tencent.femas.jvm.monitor.utils.*;
import one.profiler.AsyncProfiler;
import org.apache.commons.codec.binary.Base64;

import javax.management.MBeanServer;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPOutputStream;


public class HotspotCommandProcessor {
    private static final Logger LOGGER = Logger.getLogger(HotspotCommandProcessor.class);
    private static final String INDENT = "    ";
    private static final int MAX_FRAMES = 8;
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;


    private static ThreadMXBean threadBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
    private static RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private static ConcurrentHashMap<String, TaskInfo> taskMap = new ConcurrentHashMap<String, TaskInfo>();
    private static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

    private static TaskStatus JFG_STATUS = TaskStatus.AVAILABLE;
    private static final Object JFG_MONITOR = new Object();
    private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private static String JFG_DATA_PATH = "JavaFlameGraph.data";
    private static String HEAPDUMP_DEFAULT_FILE_NAME = "HeapDump";
    private static String JMAPTMP_PATH = "jmap.txt";
    private static String VMFLAGS_PATH = "vmflag.txt";
    private static String GCLOGREPORT_PATH = "report.txt";
    private static int JHAT_SERVER_PORT = 7000;
    public static String METHOD_TRACE_DUMP_FILE = "method_trace_dump.bin";
    private static File methodProfileFile;

    private static final int JFG_EXEC_INTERVAL = 5 * 1000; // seconds between two collections for result handling
    private static long JFG_LAST_EXEC_TIME = 0;
    private static final long JFG_DATASIZE_LIMIT = 2 * MB;
    private static final long MININAL_JFG_DATASIZE_LIMIT = 1 * KB; // 1k is too small
    private static String pid = null;
    private static int defaultTopThreads = 5;
    private static final int TOP_INTERVAL =  500; // ms between  two collection to calculate jtop info
    private static final int THREAD_UTIL_INTERVAL_NS = 50000;//100000; // 1ms

    private static final String EVENT_CPU = "cpu";
    private static final String EVENT_ALLOC = "alloc";
    private static final String EVENT_LOCK = "lock";
    private static final String EVENT_WALL = "wall";
    private static final String EVENT_ITIMER = "itimer";
    private static ObjectMapper mapper = new ObjectMapper();
    private static HotspotGeneralMetrics metrics = new HotspotGeneralMetrics();
    private static AsyncProfiler profiler;
    private static String garbagecatLib;
    private static boolean flameDataCompression = false;

    private static final boolean DEBUG_SAVE_ORIG_DATA = false;
    private static String JFG_ORIG_DATA_PATH = "JavaFlameGraph_Orig.data";
    // private static Map<Long, TimePair> threadCpuMap;
    private static volatile boolean NativeLibsLoaded = false;
    private static volatile boolean GarbagecatLibLoaded = false;
    private static ThreadInfoDrawData lastDrawData;

    static {
       // mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        if (threadBean.isThreadAllocatedMemorySupported()) {
            if (!threadBean.isThreadAllocatedMemoryEnabled()) {
                threadBean.setThreadAllocatedMemoryEnabled(true);
            }
        } else {
            LOGGER.warn("Jvm does not support get thread allocated bytes");
        }

        if (threadBean.isThreadCpuTimeSupported()) {
            if (!threadBean.isThreadCpuTimeEnabled()) {
                threadBean.setThreadCpuTimeEnabled(true);
            }
        } else {
            LOGGER.warn("Jvm does not support get thread CPU time");
        }

        JFG_DATA_PATH = JvmMonitorUtils.getDataSavePath() + JFG_DATA_PATH;
        if (DEBUG_SAVE_ORIG_DATA) {
            JFG_ORIG_DATA_PATH = JvmMonitorUtils.getDataSavePath() + JFG_ORIG_DATA_PATH;
        }
        // threadCpuMap = new HashMap<>();
    }

    public static synchronized void loadNativeLibs() {
        if (NativeLibsLoaded) {
            LOGGER.warn("Native code libraries already loaded");
            return;
        }

        String nPath = getNativeLibPath();
        try {
            loadJarDll(nPath);
            NativeLibsLoaded = true;
        } catch (Exception e) {
            LOGGER.error("Native code library failed to load.\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void loadJarDll(String name) throws Exception {
        long enterTime = System.currentTimeMillis();
        String nativeLibPath = JvmMonitorUtils.getFileFromJar(name);
        LOGGER.debug("Start Loading native library: " + name);
        profiler = AsyncProfiler.getInstance(nativeLibPath);
        long duration = System.currentTimeMillis() - enterTime;
        LOGGER.debug("Loaded native library: " + name + " takes: " + duration + " ms.");
    }

    public static synchronized void loadGarbagecatLib() {
        if (GarbagecatLibLoaded) {
            LOGGER.warn("Garabgecat lib already loaded");
            return;
        }

        String nPath = getGarbagecatLibPath();
        try {
            garbagecatLib = JvmMonitorUtils.getFileFromJar(nPath);
            GarbagecatLibLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String getGarbagecatLibPath() {
        final String libName = "garbagecat";
        String suffix = ".jar";
        final String parentFolder = "garbagecat_lib/";
        return parentFolder + libName + suffix;
    }

    private static String getNativeLibPath() {
        // TODO: support 32bits machine.
        final String libName = "libasyncProfiler";
        final String parentFolder = "native_libs/";
        String suffix = "";
        String platformName = "";
        if (OSChecker.isMac()) {
            suffix = ".so";
            platformName = "macos_x86_64";
        } else if (OSChecker.isSolaris() || OSChecker.isUnix()) {
            suffix = ".so";
            if (OSChecker.isAarch64()) {
                platformName = "linux_aarch64";
            } else {
                platformName = "linux_x86_64";
            }
        } else {
            // windows
            suffix = ".dll";
            platformName = "windows_x86_64";
        }

        return parentFolder + platformName + "/" + libName + suffix;
    }

    private static class FlameGraphCollectionThread extends Thread {
        private Command command;

        FlameGraphCollectionThread(Command comm) {
            super.setName("FlameGraphCollectionThread");
            command = comm;
        }

        @Override
        public void run() {
            LOGGER.debug("start Thread: " + this.getName());
                // This lock is used for limit collection threads
                // as well as status update.
            synchronized (JFG_MONITOR) {

                JFG_STATUS = TaskStatus.BUSY;
                updateTaskStatus(command.getTaskId(), JFG_STATUS, TaskStatusInfo.BUSY);
                TaskStatusInfo resInfo;
                String argString = command.getMetaInfo();
                FlameGraphArguments args = null;
                try {
                    args = mapper.readValue(argString,FlameGraphArguments.class);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.warn("Can not parse flame graph arguments, will use default ones.");
                    args = null;
                }

                resInfo = collectJavaFlameGraph(args, command);
                if (resInfo != TaskStatusInfo.SUCCESS_NULL) {
                    LOGGER.error("TaskId: " + command.getTaskId() + " " + "Fail collect flame graph data");
                    JFG_STATUS = TaskStatus.ERROR;
                } else {
                    LOGGER.info("success collect flame graph data for task: " + command.getTaskId());
                    JFG_STATUS = TaskStatus.COMPLETED;
                    JFG_LAST_EXEC_TIME = new Date().getTime();
                }
                updateTaskStatus(command.getTaskId(), JFG_STATUS, resInfo);
            }
        }
    }

    public static TaskStatusInfo collectJavaFlameGraph(FlameGraphArguments args, Command command) {
        int duration = 5;  // 5 seconds
        String event = EVENT_CPU;
        String filename = JFG_DATA_PATH;
        String tempFileName = null;
        boolean jsonSupport = true;
        boolean compressSupport = false;
        long dataSizeLimit = JFG_DATASIZE_LIMIT;
        TaskStatusInfo ret = TaskStatusInfo.SUCCESS_NULL;

        File tmpDataFile = null; // "./JavaFlameGraph_" + pid + ".data";
        File jsonFile = null;
        try {
            tmpDataFile = File.createTempFile("JavaFlameGraph_", ".data");
            tmpDataFile.deleteOnExit();
        } catch (IOException e) {
            LOGGER.error("TaskId: " + command.getTaskId() + " fail create temp file for JFG collection");
            e.printStackTrace();
            return TaskStatusInfo.NO_FILE;
        }

        tempFileName = tmpDataFile.getAbsolutePath();
        LOGGER.info("Temp file for flame graph: " + tempFileName);

        // process args.
        // TODO: try catch.
        if (args != null) {
            int dur = args.getDuration();
            duration = isLegalDuration(duration) ? dur : duration;
            String evt = args.getEvent();
            event = isLegalFlameGraphEvent(evt) ? evt : event;
            if (event.equalsIgnoreCase("latency")) {
                event = EVENT_WALL;
            }

            // TODO. fix bug here. and for query
            String fn = args.getFilename();
            filename = idLegalFilename(fn) ? fn : filename;

            String dataSizeLimitString = args.getDataSizeLimit();
            if (dataSizeLimitString == null) {
                dataSizeLimit = JFG_DATASIZE_LIMIT;
            } else {
                int length = dataSizeLimitString.length();
                if (length == 0) {
                    dataSizeLimit = JFG_DATASIZE_LIMIT;
                } else {
                    LOGGER.debug("data size limit string is " + dataSizeLimitString);
                    try {
                        char u = dataSizeLimitString.charAt(length - 1);
                        long unit = 1;
                        if (u == 'k' || u == 'K') {
                            unit = KB;
                        } else if (u == 'm' || u == 'M') {
                            unit = MB;
                        }
                        String sub = dataSizeLimitString.substring(0, length - 1);
                        LOGGER.debug("data size limit to parse is: " + sub);
                        LOGGER.debug("data size limit parsed unit is: " + unit);
                        dataSizeLimit = Long.parseLong(sub);
                        dataSizeLimit = dataSizeLimit * unit;
                    } catch (Exception e) {
                        LOGGER.error("wrong data size limit args: " + dataSizeLimitString);
                        dataSizeLimit = JFG_DATASIZE_LIMIT;
                    }
                    LOGGER.debug("Parsed data size limit is " + dataSizeLimit);
                }
                if (dataSizeLimit < MININAL_JFG_DATASIZE_LIMIT) {
                    LOGGER.warn("parsed data limit too small, reset to default value: " + JFG_DATASIZE_LIMIT);
                    dataSizeLimit = JFG_DATASIZE_LIMIT;
                }
            }

            String jsonSupportString = args.getJsonSupport();
            if (jsonSupportString == null) {
                jsonSupport = true;
            } else if (jsonSupportString.equalsIgnoreCase("false")) {
                jsonSupport = false;
            }

            boolean compress = args.isCompression();
            if (!compress) {
                flameDataCompression = false;
            } else {
                flameDataCompression = true;
            }
        } else {
            LOGGER.warn("conduct java flame graph with default configuration");
        }

        if (event.equalsIgnoreCase(EVENT_CPU) && !OSChecker.isUnix()) {
            LOGGER.warn("event " + EVENT_CPU + "is not suppoted because of perf unavailable, use event "
                    + EVENT_WALL);
            event = EVENT_WALL;
        }

       // String arguments = "collapsed,cstack,dot,sig,ann,file=" + filename; // + threads?
        String arguments;
        if (!event.equalsIgnoreCase(EVENT_ALLOC)) {
            arguments = "collapsed,cstack,dot,sig,file=" + tempFileName; // + threads?
        } else {
            int topN = 20;
            LOGGER.debug("FlameGraph of Allocation only dump top 20 symbols");
            arguments = "collapsed,dot,sig,topN=" + topN + ",file=" + tempFileName;
        }

        String startCmd = "event=" + event + "," + "start," + arguments;
        String stopCmd = "event=" + event + "," + "stop," + arguments;
        // LOGGER.info("ZLIN- Start collecting: " + startCmd + "duration: " + duration);
        LOGGER.debug("Start collecting: " + startCmd + " duration: " + duration);
        try {
            profiler.execute(startCmd);
            Thread.sleep((duration + 1) * 1000);
            profiler.execute(stopCmd);
            LOGGER.debug("Stop collect: " + stopCmd);
        } catch (Exception e) {
            LOGGER.error("TaskId: " + command.getTaskId() + " Flame Graph Can not collected: " + startCmd);
            e.printStackTrace();
            return TaskStatusInfo.NO_FILE;
        }

        if (!tmpDataFile.exists() || !tmpDataFile.isFile()) {
            LOGGER.error("TaskId: " + command.getTaskId() + " " + "collect Finish with no file generated");
            return TaskStatusInfo.NO_FILE;
        }

        String rawData = readFileToString(tempFileName);
        // tmp data file can be deleted now
        if (tmpDataFile.exists()) {
            LOGGER.info("try to delete file: " + tmpDataFile.getName());
            if (tmpDataFile.delete()) {
               LOGGER.info("successfully delete flame graph temp file");
            } else {
                LOGGER.warn("fail delete flame graph temp file: " + tempFileName);
            }
        }

        if (rawData.length() == 0) {
            LOGGER.error("TaskId: " + command.getTaskId() + " collect Finish with no file content");
            return TaskStatusInfo.NO_DATA;
        }
        LOGGER.debug("rawData size is :" + rawData.length() + " limit is: " + dataSizeLimit);
        // LOGGER.debug("rawdata: " + rawData);

        String dataToWrite = rawData;
        if (jsonSupport) {
            String jsonData = null;
            try {
                jsonData = FlameGraphUtil.parseJsonFromString(rawData);
            } catch (IOException e) {
                LOGGER.error("TaskId: " + command.getTaskId() + " Failed to transfer data to json");
                return TaskStatusInfo.FAIL_TRANSFER;
            }
            if (jsonData != null) {
                LOGGER.debug("jsonData size is :" + jsonData.length());
            }
            dataToWrite = jsonData;

            if (flameDataCompression) {
                dataToWrite = compressAndEncode(jsonData);
            }
            //  LOGGER.debug("jsonData: " + jsonData);
            if (dataToWrite != null && dataToWrite.length() >= dataSizeLimit) {
                LOGGER.error("TaskId: " + command.getTaskId() + " "
                        + "Collected Flame graph compressed data size too large: "
                        + dataToWrite.length() + " raw size: " + rawData.length());
                return TaskStatusInfo.EXCEED_SIZE;
            }
        } else {
            if (rawData.length() >= dataSizeLimit) {
                LOGGER.error("TaskId: " + command.getTaskId()
                        + " Collected Flame graph raw file size too large: " + rawData.length());
                return TaskStatusInfo.EXCEED_SIZE;
            }
            LOGGER.info("write Raw flame graph Data, size: " + dataToWrite.length());
        }

        readWriteLock.writeLock().lock();
        File f = new File(filename);
        LOGGER.debug("filename: " + filename + " dataToWrite is " + dataToWrite);
        if (dataToWrite != null && !writeStringToFile(f, dataToWrite)) {
            LOGGER.error("TaskId: " + command.getTaskId() + " Failed to transfer data to json");
            ret = TaskStatusInfo.FAIL_TRANSFER;
        }
        if (DEBUG_SAVE_ORIG_DATA) {
            File of = new File(JFG_ORIG_DATA_PATH);
            if (!writeStringToFile(of, rawData)) {
                LOGGER.error("TaskId: " + command.getTaskId() + " Failed to write original data to file");
            }
        }
        readWriteLock.writeLock().unlock();
        // return result. only the file name or the whole data?
        // String fileData = readFileToString(filename);
        // LOGGER.debug("flameGraph file content: " + fileData + " size: " + fileData.length());
        // lastCollectedFlameGraphData = filename;
        return ret;

    }

    private static String compressAndEncode(String originString) {
        if (originString == null || originString.length() == 0) {
            return originString;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(originString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Base64 base64encoder = new Base64();
        return base64encoder.encodeBase64String(out.toByteArray());
    }
    
    public static ResultPackage processJavaFlameGraphRequest(Command cmd) {
        String action = cmd.getAction();
        String metaInfo = cmd.getMetaInfo();
        String data = "";
        ResultInfo rInfo;
        LOGGER.debug("Start Process flame graph request: " + cmd);
        HotspotCommandProcessor.loadNativeLibs();
        // TODO. move to sub method.
        if (action.equalsIgnoreCase("collect")) {
            switch (JFG_STATUS) {
                case ERROR: {
                    LOGGER.warn("Collect Flame Graph with ERROR status");
                } // fall through
                case COMPLETED: {
                    // Dup with available,
                    // TODO: consider report error if time interval is too small.
                    if (JFG_LAST_EXEC_TIME == 0) {
                        // fall through, fist time collection
                    } else if (new Date().getTime() - JFG_LAST_EXEC_TIME > JFG_EXEC_INTERVAL) {
                        // fall through
                    } else {
                        // Warning or Error?
                        LOGGER.warn("Collect Flame Graph too frequently");
                        // continue or interrupt?
                    }
                    // fall through
                } // fall through
                case AVAILABLE: {
                    taskMap.put(cmd.getTaskId(), new TaskInfo(cmd, TaskStatus.BUSY, TaskStatusInfo.BUSY));
                    FlameGraphCollectionThread t = new FlameGraphCollectionThread(cmd);
                    t.setDaemon(true);
                    t.setContextClassLoader(HotspotCommandProcessor.class.getClassLoader());
                    t.start();
                    rInfo = new ResultInfo(TaskStatus.BUSY.name());
                } break;
                case BUSY:
                    // return BUSY!
                    data = "taskId: " + cmd.getTaskId() + " BUSY collecting java flame graph data";
                    rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.BUSY.name(), data);
                    break;
                default:
                    data = "taskId: " + cmd.getTaskId() + " Unknown Flame graph collection status: " + JFG_STATUS;
                    LOGGER.error(data);
                    rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), data);
            }
        }
        else if (action.equalsIgnoreCase("query")) {
            LOGGER.debug("process query: " + cmd);
            // acquire read lock to block write while reading. or block read while writing.
            String targetTaskId = cmd.getTaskId();
            TaskInfo taskInfo = getTaskInfo(targetTaskId);
            if (taskInfo == null) {
                data = "No task with taskId: " + targetTaskId + " found";
                LOGGER.error(data);
                rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.NO_DATA.name(), data);
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }
            TaskStatus targetTaskStatus = taskInfo.getStatus();
            // TaskStatusInfo targetTaskStatusInfo = taskInfo.getTaskStatusInfo();
            FlameGraphQueryMetaInfo requireData = null;
            try {
                requireData = mapper.readValue(metaInfo, FlameGraphQueryMetaInfo.class);
            } catch (IOException e) {
                LOGGER.error("TaskId: " + targetTaskId + " Can not parse metaInfo of requireData");
                e.printStackTrace();
                rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), "wrong query metaInfo");
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }

            if (targetTaskStatus == TaskStatus.COMPLETED) {
                LOGGER.debug("acquire lock......");
                readWriteLock.readLock().lock();
                LOGGER.debug("acquire lock ......Pass");
                if (requireData != null && requireData.getRequireData() != null
                        && requireData.getRequireData().equalsIgnoreCase("TRUE")) {
                    LOGGER.debug("testing file: " + JFG_DATA_PATH);
                    // TODO  move to collection side.
                    String retMsg1 = readFileToString(JFG_DATA_PATH);
                    if (flameDataCompression) {
                        data = retMsg1;
                    } else {
                        data = retMsg1;
                    }
                    rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", data, flameDataCompression);
                } else {
                    LOGGER.debug("No required data!");
                    rInfo = new ResultInfo(TaskStatus.COMPLETED.name());
                }
                LOGGER.debug("release lock......");
                readWriteLock.readLock().unlock();
                LOGGER.debug("release lock......Pass");
            } else if (targetTaskStatus == TaskStatus.ERROR) {
                TaskStatusInfo ts = taskInfo.getTaskStatusInfo();
                rInfo = new ResultInfo(TaskStatus.ERROR.name(), ts.name(), "");
            } else if (targetTaskStatus == TaskStatus.BUSY) {
                rInfo = new ResultInfo(TaskStatus.BUSY.name());
            } else {
                // Should not be here!
                data = "TaskId: " + targetTaskId + " Unreachable targetTaskStatus: " + targetTaskStatus.name();
                rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), data);
            }
        }
        else {
            LOGGER.error("Unreachable action: " + action);
            data = "TaskId: " + cmd.getTaskId() + " Unknown Flame graph action: " + action;
            LOGGER.error(data);
            rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), data);
        }
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static String getPid() {
        if (pid == null) {
            String jvmName = runtimeBean.getName();
            LOGGER.debug("get Pid: " + jvmName);
            pid = jvmName.split("@")[0];
        }
        return pid;
    }

    public static ResultPackage getProcessId(Command cmd) {
        ResultInfo rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", getPid());
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static ResultPackage getHeapDump(Command cmd) {
        HeapDumperArguments args = null;
        String fileName = null;
        String path;
        String metaString = cmd.getMetaInfo();
        String action = cmd.getAction();

        if (metaString.equalsIgnoreCase("") || metaString == null) {
            LOGGER.info("metaInfo is null, use default options");
        } else {
            try {
                args = mapper.readValue(metaString, HeapDumperArguments.class);
                fileName = args.getFilename();
            } catch (IOException e) {
                e.printStackTrace();
                args = null;
                LOGGER.warn("Can not parse heapdump arguments, will use default ones.");
            }
        }

        if (fileName == null || fileName == "") {
            path = JvmMonitorUtils.getDataSavePath() + HEAPDUMP_DEFAULT_FILE_NAME + ".hprof";
        } else {
            path = fileName + ".hprof";
        }

        if (action.equalsIgnoreCase("collect")) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            HeapDumper.dumpHeap(path, true);
            // just start heapdump
            ResultInfo rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), TaskStatusInfo.SUCCESS_NULL.name(),
                    "start Heapdump");
            return new ResultPackage(cmd.getTaskId(), rInfo);
        } else if (action.equalsIgnoreCase("query")) {
            File heapdumpFile = new File(path);
            ResultInfo rInfo;
            if (heapdumpFile.length() == 0) {
                rInfo = new ResultInfo(TaskStatus.BUSY.name(), TaskStatusInfo.BUSY.name(), "");
                return new ResultPackage(cmd.getTaskId(), rInfo);
            } else {
                rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), TaskStatusInfo.SUCCESS_NULL.name(), path);
            }
            return new ResultPackage(cmd.getTaskId(), rInfo);
        } else {
            LOGGER.error("Heapdump action is wrong!");
            ResultInfo rInfo = new ResultInfo(TaskStatus.ERROR.name(),
                    TaskStatusInfo.MAL_FUNC.name(), "Heapdump action is wrong");
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }
    }

    public static ResultPackage getThreadDump(Command cmd) {

        boolean compressThreadInfo = false;
        boolean needDrawData = false;
        ThreadInfoArguments args = null;
        String metaString = cmd.getMetaInfo();
        LOGGER.debug("thread dump : metaString " + metaString);
        if (metaString.equalsIgnoreCase("") || metaString == null) {
            compressThreadInfo = false;
            needDrawData = false;
        } else {
            try {
                args = mapper.readValue(metaString, ThreadInfoArguments.class);
                compressThreadInfo = args.isCompression();
                needDrawData = args.isDrawdata();
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Can not parse jstack arguments, will use default ones.");
                compressThreadInfo = false;
                args = null;
            }
        }

        // don't collect thread dump for draw data. only collect when there is no draw data.
        if (needDrawData && (lastDrawData != null)) {
            // return lastDrawData.
            String threadDrawDataSB = null;
            try {
                threadDrawDataSB = mapper.writeValueAsString(lastDrawData);
                if (compressThreadInfo) {
                    LOGGER.debug("compress draw data");
                    threadDrawDataSB = compressAndEncode(threadDrawDataSB);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                String errMsg = "Error at writing thread Draw data to json";
                LOGGER.error(errMsg);
                ResultInfo rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), errMsg);
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }
            ResultInfo rInfo = null;
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", threadDrawDataSB, compressThreadInfo);
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }

        ThreadInfo[] threadInfos;
        // long start = System.currentTimeMillis();
        threadInfos = threadBean.dumpAllThreads(true, false);
        // long phase1 = System.currentTimeMillis();
        //LOGGER.info("dump All threads took " + (phase1 - start)/1000.0 + "s");
        ThreadInfoList<ThreadInfoEntry> tiList = new ThreadInfoList<ThreadInfoEntry>();
        if (!needDrawData || lastDrawData == null) {
            // collect drawdata every time we collect thread dump.
            ThreadInfoDrawData drawData = new ThreadInfoDrawData();
            for (ThreadInfo info : threadInfos) {
                tiList.add(makeThreadInfoEntry(info, compressThreadInfo, drawData));
            }
            lastDrawData = drawData;
        }

        if (needDrawData && lastDrawData != null) {
            // return lastDrawData.
            String threadDrawDataSB = null;
            try {
                threadDrawDataSB = mapper.writeValueAsString(lastDrawData);
                if (compressThreadInfo) {
                    LOGGER.debug("compress draw data");
                    threadDrawDataSB = compressAndEncode(threadDrawDataSB);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                String errMsg = "Error at writing thread Draw data to json";
                LOGGER.error(errMsg);
                ResultInfo rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), errMsg);
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }

            ResultInfo rInfo = null;
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", threadDrawDataSB, compressThreadInfo);
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }

        String threadDumpSB = null;

        try {
            threadDumpSB = mapper.writeValueAsString(tiList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            String errMsg = "Error at writting thread Dump to json";
            LOGGER.error(errMsg);
            ResultInfo rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), errMsg);
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }

        LOGGER.debug("thread dump info:" + threadDumpSB);
        ResultInfo rInfo = null;
        rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", threadDumpSB, compressThreadInfo);
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    private static ThreadInfoEntry makeThreadInfoEntry(ThreadInfo info, boolean compress, ThreadInfoDrawData drawData) {

        ThreadInfoEntry entry = new ThreadInfoEntry();
        PerThreadDrawData perThreadDrawData = new PerThreadDrawData();

        entry.setThreadName(info.getThreadName());
        entry.setThreadState(info.getThreadState().name());

        perThreadDrawData.setThreadName(info.getThreadName());
        perThreadDrawData.setThreadState(info.getThreadState());
        perThreadDrawData.setNative(info.isInNative());

        long allocatedBytes = 0;
        allocatedBytes = threadBean.getThreadAllocatedBytes(info.getThreadId());
        entry.setThreadAllocatedBytes(Long.toString(allocatedBytes));

        long lastThreadTime = threadBean.getThreadCpuTime(info.getThreadId());
        // Todo, use the latest one?
        entry.setThreadCpuTime(Long.toString(lastThreadTime));
        long lastNanoTime = System.nanoTime();
        // sleep for cpu util
        try {
            Thread.sleep(0,THREAD_UTIL_INTERVAL_NS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long threadCpuTime = threadBean.getThreadCpuTime(info.getThreadId());
        long now = System.nanoTime();

        LOGGER.debug("thread: " + info.getThreadId() + " thread cpu time: " + threadCpuTime
                + " lastThreadTime: " + lastThreadTime
                + " now: " + now + " lasttimestamp: " + lastNanoTime);

        double threadCpuUtil = ((double)(threadCpuTime - lastThreadTime)) / (now - lastNanoTime);
        if (threadCpuUtil < 0.001) {
            threadCpuUtil = 0.0;
        }

        entry.setThreadCpuUtil(Double.toString(threadCpuUtil * 100.0));

        entry.setThreadBlockCount(Long.toString(info.getBlockedCount()));

        StringBuilder sb = new StringBuilder(" Id=" + info.getThreadId());

        if (info.getLockName() != null) {
            sb.append(" on " + info.getLockName());
        }
        if (info.getLockOwnerName() != null) {
            sb.append(" owned by \"" + info.getLockOwnerName()
                    + "\" Id=" + info.getLockOwnerId());
        }
        if (info.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (info.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');

        StackTraceElement[] stackTrace = info.getStackTrace();
        if (stackTrace.length > 0) {
            perThreadDrawData.setMethodInfo(stackTrace[0].toString());
        } else {
            perThreadDrawData.setMethodInfo("");
        }
        StringBuilder traceSB = new StringBuilder("");
        int i = 0;
        for (; i < stackTrace.length && i < MAX_FRAMES; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append(" at " + ste.toString());
            sb.append('\n');

            traceSB.append(" at " + ste.toString());
            traceSB.append('\n');

            if (i == 0 && info.getLockInfo() != null) {
                Thread.State ts = info.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append(" -  blocked on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                    case TIMED_WAITING:
                        sb.append(" -  waiting on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }
            MonitorInfo[] lockedMonitors = info.getLockedMonitors();
            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append(" -  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append(" ...");
            sb.append('\n');

            traceSB.append(" ...");
            traceSB.append('\n');
        }
        perThreadDrawData.setTraceInfo(traceSB.toString());

        LockInfo[] locks = info.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n    Number of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append(" - " + li);
                sb.append('\n');
            }
        }
        sb.append("\n");
        String rawInfo =  sb.toString();
        if (compress) {
            String compressed = compressAndEncode(rawInfo);
            entry.setThreadInfos(compressed);
        } else {
            entry.setThreadInfos(rawInfo);
        }
        drawData.update(perThreadDrawData);
        return entry;
    }

    public static ResultPackage getDeadLockInfo(Command cmd) {
        long[] tids;
        StringBuilder lockMsg = new StringBuilder();
        ResultInfo rInfo;
        String retMsg = "";
        DeadLockInfo dlinfo = new DeadLockInfo();
        if (threadBean.isSynchronizerUsageSupported()) {
            tids = threadBean.findDeadlockedThreads();
            if (tids == null) {
                // lockMsg.append("no dead lock detected.");
                rInfo = new ResultInfo(TaskStatus.COMPLETED.name());
                return new ResultPackage(cmd.getTaskId(), rInfo);
            } else {
                ThreadInfo[] infos = threadBean.getThreadInfo(tids, true, true);
                for (ThreadInfo ti : infos) {
                    lockMsg.append("\"" + getThreadInfo(ti))
                            .append(getMonitorInfo(ti))
                            .append(getLockInfo(ti.getLockedSynchronizers()))
                            .append("\"")
                            .append('\n');
                }
                LOGGER.debug("dead lock info: " + lockMsg.toString());
                dlinfo.setLockInfos(lockMsg.toString());
                dlinfo.setThreadCount(infos.length);
                // lockMsg.append(", \" threadCount\" : ").append(infos.length).append(" }");
            }
        } else {
            tids = threadBean.findMonitorDeadlockedThreads();
            if (tids == null) {
                // lockMsg.append("no dead lock detected.");
                rInfo = new ResultInfo(TaskStatus.COMPLETED.name());
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }

            ThreadInfo[] infos = threadBean.getThreadInfo(tids, Integer.MAX_VALUE);
            for (ThreadInfo ti : infos) {
                lockMsg.append("\"" + getThreadInfo(ti) + "\"").append('\n');
            }
            LOGGER.debug("dead lock info: " + lockMsg.toString());
            dlinfo.setLockInfos(lockMsg.toString());
            dlinfo.setThreadCount(infos.length);

            //lockMsg.append(", \" threadCount\" : ").append(infos.length).append(" }");
        }

        try {
            retMsg = (dlinfo.getThreadCount() == 0) ? "" : mapper.writeValueAsString(dlinfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), retMsg);
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }
        rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", retMsg);
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    private static String getLockInfo(LockInfo[] locks) {
        StringBuilder sb = new StringBuilder(INDENT
                + "Locked synchronizers: count = " + locks.length + "\n");
        for (LockInfo li : locks) {
            sb.append(INDENT + "  - " + li + "\n");
        }
        return sb.toString();
    }

    private static String getMonitorInfo(ThreadInfo ti) {
        StringBuilder sb = new StringBuilder("");
        MonitorInfo[] monitors = ti.getLockedMonitors();
        sb.append(INDENT + "Locked monitors: count = " + monitors.length + "\n");
        for (MonitorInfo mi : monitors) {
            sb.append(INDENT + "  - " + mi + " locked at \n");
            sb.append(INDENT + "      " + mi.getLockedStackDepth()
                    + " " + mi.getLockedStackFrame() + "\n");
        }
        return sb.toString();
    }

    private static String getThreadInfo(ThreadInfo ti) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\""
                + " Id=" + ti.getThreadId() + " in " + ti.getThreadState());
        if (ti.getLockName() != null) {
            sb.append(" on lock=" + ti.getLockName());
        }
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        sb.append("\n");
        if (ti.getLockOwnerName() != null) {
            sb.append(INDENT + " owned by " + ti.getLockOwnerName() + " Id=" + ti.getLockOwnerId() + "\n");
        }
        return sb.toString();
    }

    public static String readFileToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(filecontent);
        } catch (FileNotFoundException e) {
            LOGGER.error("Error read data from file: " + fileName);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("Error read data from file: " + fileName);
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("Error close input stream for file: " + fileName);
                    e.printStackTrace();
                }
            }
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeStringToFile(File file, String string) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            byte[] bytesArray = string.getBytes("8859_1");
            fos.write(bytesArray);
            fos.flush();
        } catch (IOException e) {
            LOGGER.error("Fail write file: " + file.getName());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LOGGER.error("Fail close file: " + file.getName());
                return false;
            }
        }
        return true;
    }

    private static boolean idLegalFilename(String fn) {
        // TODO: more strict check of file path.
        if (fn == null || fn.isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean isLegalFlameGraphEvent(String evt) {
        if (evt == null || evt.isEmpty()) {
            return false;
        }
        if (evt.equalsIgnoreCase("cpu")
                || evt.equalsIgnoreCase("alloc")
                || evt.equalsIgnoreCase("lock")
                || evt.equalsIgnoreCase("latency")
                || evt.equalsIgnoreCase("wall")
        ) {
            return true;
        }
        return true;
    }

    private static boolean isLegalDuration(int dur) {
        if (dur > 0) {
            return true;
        }
        return false;
    }

    // TODO. throw exception
    public static void updateTaskStatus(String taskId, TaskStatus status, TaskStatusInfo info) {
        TaskInfo tInfo = taskMap.get(taskId);
        if (tInfo == null) {
            LOGGER.error("Can not find taskInfo of taskId:" + taskId);
        } else {
            tInfo.setStatus(status, info);
        }
    }

    public static TaskInfo getTaskInfo(String taskId) {
        TaskInfo tInfo = taskMap.get(taskId);
        if (tInfo == null) {
            return null;
        } else {
            return tInfo;
        }
    }


    private static ResultPackage getGeneralMetrics(Command cmd) {
        String retMsg = null;

        metrics.collect();
        TaskStatus status = TaskStatus.ERROR;
        try {
            retMsg = mapper.writeValueAsString(metrics);
            status = TaskStatus.COMPLETED;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            LOGGER.error("Can not convert metrics to JSON");
            retMsg = null;
            status = TaskStatus.ERROR;
        }
        ResultInfo ri = new ResultInfo(status.name(), "",  retMsg);
        return new ResultPackage(cmd.getTaskId(),ri);
    }

    private List<Map.Entry<Long, ThreadInfo>> getTopThreadList(int topThreads) {
        boolean enabledCpu = false;
        try {
            if (threadBean.isThreadCpuTimeSupported()) {
                if (!threadBean.isThreadCpuTimeEnabled()) {
                    enabledCpu = true;
                    threadBean.setThreadCpuTimeEnabled(true);
                }
            } else {
                LOGGER.error("MBean doesn't support thread CPU Time");
                return null;
            }
            ThreadInfo[] allThreadInfos;
            allThreadInfos = threadBean.dumpAllThreads(true, false);
            Map<Long, JTopThreadInfo> threadInfos = new HashMap<>();
            for (ThreadInfo info : allThreadInfos) {
                long threadId = info.getThreadId();
                if (Thread.currentThread().getId() == threadId) {
                    continue;
                }
                long cpuTime = threadBean.getThreadCpuTime(threadId);
                if (cpuTime == -1) {
                    continue;
                }
                if (info == null) {
                    continue;
                }
                threadInfos.put(threadId, new JTopThreadInfo(cpuTime, info));
            }

            try {
                Thread.sleep(TOP_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            for (ThreadInfo info : allThreadInfos) {
                long threadId = info.getThreadId();
                if (Thread.currentThread().getId() == threadId) {
                    continue;
                }

                long cpuTime = threadBean.getThreadCpuTime(threadId);
                if (cpuTime == -1) {
                    threadInfos.remove(threadId);
                    continue;
                }
                if (info == null) {
                    threadInfos.remove(threadId);
                    continue;
                }
                JTopThreadInfo data = threadInfos.get(threadId);
                if (data != null) {
                    data.setDelta(cpuTime, info);
                } else {
                    threadInfos.remove(threadId);
                }
            }
            List<JTopThreadInfo> jtis = new ArrayList<>(threadInfos.values());
            SortedMap<Long, ThreadInfo> map = new TreeMap<Long, ThreadInfo>();
            for (int i = 0; i < jtis.size(); i++) {
                JTopThreadInfo jti = jtis.get(i);
                long cpuRatio = jti.cpuTime / (TOP_INTERVAL * 10000);
                if (cpuRatio != 0) {
                  map.put(new Long(cpuRatio), jti.info);
                }
            }

            // build the thread list and sort it with cpuRatio
            // in decreasing order
            Set<Map.Entry<Long, ThreadInfo>> set = map.entrySet();
            List<Map.Entry<Long, ThreadInfo>> list =
                new ArrayList<Map.Entry<Long, ThreadInfo>>(set);
            Collections.reverse(list);
            final int tops = Math.min(topThreads, list.size());
            List<Map.Entry<Long, ThreadInfo>> subList = list.subList(0, tops);
            return subList;
        } finally {
            if (enabledCpu) {
                threadBean.setThreadCpuTimeEnabled(false);
            }
        }
    }

    private static boolean isLegalTopThreads(int topThreads) {
      if (topThreads > 0) {
          return true;
      }
      return false;
    }

    private static ResultPackage getJTopInfo(Command cmd) {
        String argString = cmd.getMetaInfo();
        JTopArguments args = null;
        int topThreads = defaultTopThreads;
        try {
            args = mapper.readValue(argString, JTopArguments.class);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Can not parse jtop arguments, will use default ones.");
            args = null;
        }
        if (args != null) {
            int tt = args.getTopThreads();
            topThreads = isLegalTopThreads(tt) ? tt : topThreads;
        }

        HotspotCommandProcessor hcp = new HotspotCommandProcessor();
        List<Map.Entry<Long, ThreadInfo>> list = hcp.getTopThreadList(topThreads);
        ThreadInfoList<JTopInfoEntry> jiList = new ThreadInfoList<JTopInfoEntry>();
        for (int i = 0; i < list.size(); i++) {
            jiList.add(makeJTopInfoEntry(list.get(i).getKey(), list.get(i).getValue()));
        }

        String jtopDumpSB = null;
        try {
            jtopDumpSB = mapper.writeValueAsString(jiList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            String errMsg = "Error at writing thread Dump to json";
            LOGGER.error(errMsg);
            ResultInfo rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), errMsg);
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }

        LOGGER.debug("jtop dump info:" + jtopDumpSB);
        ResultInfo rInfo = new ResultInfo(TaskStatus.COMPLETED.name(),jtopDumpSB);
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    private static JTopInfoEntry makeJTopInfoEntry(Long threadCPURatio, ThreadInfo info) {
        JTopInfoEntry entry = new JTopInfoEntry();
        entry.setThreadName(info.getThreadName());
        entry.setThreadCPURatio(threadCPURatio.toString() + "%");
        StringBuilder sb = new StringBuilder("TId: " + info.getThreadId());

        int i = 0;
        boolean hasDumpNative = false;
        StackTraceElement[] stackTrace = info.getStackTrace();
        for (; i < stackTrace.length && i < MAX_FRAMES; i++) {
            StackTraceElement ste = stackTrace[i];
            if (ste.isNativeMethod() && !hasDumpNative) {
              sb.append(" at " + ste.toString());
              sb.append('\n');
              hasDumpNative = true;
              continue;
            } else {
              sb.append(" at " + ste.toString());
              sb.append('\n');
              break;
            }
        }
        if (i < stackTrace.length) {
            sb.append(" ...");
            sb.append('\n');
        }
        sb.append("\n");
        entry.setThreadInfos(sb.toString());
        return entry;
    }

    public static ResultPackage processCommandInternal(Command cmd, Instrumentation inst) {
        String command = cmd.getType();
        ResultPackage rp = null;

        long enterTime = System.currentTimeMillis();
        if (cmd.getTaskId().equalsIgnoreCase("femas_getPid")) {
            LOGGER.debug("enter: process command internal: " + cmd);
        } else {
            LOGGER.info("enter: process command internal: " + cmd);
        }

        if (command.equalsIgnoreCase("ThreadDump")) {
            rp = getThreadDump(cmd);
        } else if (command.equalsIgnoreCase("DeadLockDetect")) {
            rp = getDeadLockInfo(cmd);
        } else if (command.equalsIgnoreCase("JavaFlameGraph")) {
            rp = processJavaFlameGraphRequest(cmd);
        } else if (command.equalsIgnoreCase("GetPid")) {
            rp = getProcessId(cmd);
        } else if (command.equalsIgnoreCase("Metrics")) {
            rp = getGeneralMetrics(cmd);
        } else if (command.equalsIgnoreCase("JTop")) {
            rp = getJTopInfo(cmd);
        } else if (command.equalsIgnoreCase("MethodProfile")) {
            rp = processMethodProfile(cmd, inst);
        } else if (command.equalsIgnoreCase("VMFlags")) {
            rp = getVmFlags(cmd);
        } else if (command.equalsIgnoreCase("HeapDump")) {
            rp = getHeapDump(cmd);
        } else if (command.equalsIgnoreCase("GCLogAnalyze")) {
            rp = analyzeGCLog(cmd);
        } else if (command.equalsIgnoreCase("JMapHisto")) {
            rp = jmapHisto(cmd);
        } else if (command.equalsIgnoreCase("")) {
            rp = jhatAnalyze(cmd);
        } else {
            LOGGER.error("Unknown Qoco Command: " + command);
            return new ResultPackage(null, null);
        }

        long duration = System.currentTimeMillis() - enterTime;
        if (cmd.getTaskId().equalsIgnoreCase("Qoco_getPid")) {
            LOGGER.debug("leave: Response package of command " + cmd
                    + " duration: " + duration + " ms"
                    + " message : " + rp);
        } else {
            if (cmd.getAction().equalsIgnoreCase("query")
                    || command.equalsIgnoreCase("ThreadDump")
                    || command.equalsIgnoreCase("DeadLockDetect")) {
                LOGGER.info("leave: Response status of command " + cmd
                        + " duration: " + duration + " ms"
                        + " message : " + rp.getResultInfo().getStatus());

            } else {
                LOGGER.info("leave: Response package of command " + cmd
                        + " duration: " + duration + " ms"
                        + " message : " + rp);
            }
        }
        return rp;
    }

    public static ResultPackage getVmFlags(Command cmd) {
        ResultInfo rInfo;
        try {
            String []command = {"/bin/sh", "-c", "jcmd " + getPid()  + " VM.flags > " +  VMFLAGS_PATH};
            Process process = null;
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            String data = readFileToString(VMFLAGS_PATH);
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", data);
        } catch (Exception e) {
            e.printStackTrace();
            rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), "wrong query metaInfo");
        }
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static ResultPackage analyzeGCLog(Command cmd) {
        GCLogAnalyzerArguments args = null;
        String reportFileName = GCLOGREPORT_PATH;
        String gcLogFile;
        String path;
        String metaString = cmd.getMetaInfo();
        ResultInfo rInfo;
        if (metaString.equalsIgnoreCase("") || metaString == null) {
            rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), "wrong query metaInfo");
        } else {
            HotspotCommandProcessor.loadGarbagecatLib();
            try {
                args = mapper.readValue(metaString, GCLogAnalyzerArguments.class);
                gcLogFile = args.getFilename();
                File logFile = new File(gcLogFile);
                if (!logFile.exists()) {
                    rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(),
                            "gc log file does not exist");
                    return new ResultPackage(cmd.getTaskId(), rInfo);
                }
                try {
                    File reportFile = new File(GCLOGREPORT_PATH);
                    if (reportFile.exists()) {
                        reportFile.delete();
                    }
                    String []command = {"/bin/sh", "-c", "java -jar " + garbagecatLib  + " "
                            + gcLogFile + " -o " + GCLOGREPORT_PATH};
                    Process process = null;
                    process = Runtime.getRuntime().exec(command);
                    process.waitFor();
                    String data = readFileToString(GCLOGREPORT_PATH);
                    rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", data);

                } catch (Exception e) {
                    e.printStackTrace();
                    rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(),
                            "analyze gc log fails");
                }
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Can not parse gclog analyze arguments, will use default ones.");
                args = null;
                rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), "wrong query metaInfo");
            }
        }
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static boolean isLocalPortUsing(int port) {
        boolean flag = true;
        try {
            flag = isPortUsing("127.0.0.1", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean isPortUsing(String host, int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress address = InetAddress.getByName(host);
        try {
            Socket socket = new Socket(address, port);
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static ResultPackage jhatAnalyze(Command cmd) {
        String action = cmd.getAction();
        ResultInfo rInfo;
        String metaString = cmd.getMetaInfo();
        if (action.equalsIgnoreCase("collect")) {
            if (isLocalPortUsing(JHAT_SERVER_PORT)) {
                KillPortServer killPortServer = new KillPortServer();
                killPortServer.start(JHAT_SERVER_PORT);
            }
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File tmpDumpFile = File.createTempFile("Heapdump_", ".hprof");
                        tmpDumpFile.delete();
                        HeapDumper.dumpHeap(tmpDumpFile.getAbsolutePath(), true);
                        while (true) {
                            if (tmpDumpFile.length() != 0) {
                                String[] command = {"/bin/sh", "-c", "jhat -J-mx512m " + tmpDumpFile.getAbsolutePath()};
                                Process process = null;
                                process = Runtime.getRuntime().exec(command);
                                process.waitFor();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", "Start jhat analyzing");
        } else if (action.equalsIgnoreCase("query")) {
            if (isLocalPortUsing(JHAT_SERVER_PORT)) {
                rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", "");
            } else {
                rInfo = new ResultInfo(TaskStatus.BUSY.name(), "", "jhat is analyzing");
            }
        } else {
            rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), "");
        }
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static ResultPackage jmapHisto(Command cmd) {
        ResultInfo rInfo;
        try {
            File file = new File(JMAPTMP_PATH);
            String []command = {"/bin/sh", "-c", "jmap -histo:live " + getPid() + " > " + JMAPTMP_PATH};
            Process process = null;
            process = Runtime.getRuntime().exec(command);
            process.waitFor();

            JmapInfoList<JmapInfo> jmapInfoList = new JmapInfoList<JmapInfo>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String str = null;
            int i = 0;
            while ((str = br.readLine()) != null) {
                if (i < 3) {
                    i++;
                    continue;
                }
                JmapInfo jmapInfo = new JmapInfo();
                String []ss = str.split("\\s+");
                if (ss.length == 6) {
                    jmapInfo.setInstances(ss[2]);
                    jmapInfo.setBytes(ss[3]);
                    jmapInfo.setClassName(ss[4] + ss[5]);
                } else if (ss.length == 5) {
                    if (ss[1].contains(":")) {
                        jmapInfo.setInstances(ss[2]);
                        jmapInfo.setBytes(ss[3]);
                        jmapInfo.setClassName(ss[4]);
                    } else {
                        jmapInfo.setInstances(ss[1]);
                        jmapInfo.setBytes(ss[2]);
                        jmapInfo.setClassName(ss[3] + ss[4]);
                    }
                } else if (ss.length == 4) {
                    jmapInfo.setInstances(ss[1]);
                    jmapInfo.setBytes(ss[2]);
                    jmapInfo.setClassName(ss[3]);
                } else {
                    continue;
                }
                jmapInfoList.add(jmapInfo);
            }
            br.close();
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", mapper.writeValueAsString(jmapInfoList));
        } catch (Exception e) {
            e.printStackTrace();
            rInfo  = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), "jmap exec failed");
        }
        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    private static ResultPackage processMethodProfile(Command cmd, Instrumentation inst) {
        // '{"taskId":"test1","type”:”MethodProfile","action”:"trace","metaInfo":"{\”args\":\”class.*;\"}"}'
        String action = cmd.getAction();
        String metaInfo = cmd.getMetaInfo();
        ResultInfo rInfo;
        String data = "";
        LOGGER.debug("Start process method profile " + cmd);
        MethodProfileMetaInfo meta;
        try {
            meta = mapper.readValue(metaInfo, MethodProfileMetaInfo.class);
        } catch (IOException e) {
            LOGGER.error("TaskId: " + cmd.getTaskId() + " Can not parse arguments of method profile");
            e.printStackTrace();
            rInfo = new ResultInfo(TaskStatus.ERROR.name(),TaskStatusInfo.MAL_FUNC.name(), "wrong query metaInfo");
            return new ResultPackage(cmd.getTaskId(), rInfo);
        }

        String path = JvmMonitorUtils.getDataSavePath() + METHOD_TRACE_DUMP_FILE;

        if (action.equalsIgnoreCase("trace")) {
            // action and args:  \
            //      "trace" {times, args} // method invocation trace
            //      "singleStep" {time, args}
            //      "profile" // method argument, return values.
            //      "Details" trace + profile.
            String options = "action=" + action + ",count=" + meta.getProfileCount() + "," + meta.getArgs();
            JvmMonitorBCIAgentOptions bciAgentOptions = new JvmMonitorBCIAgentOptions();

            // delete the former profile log file
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            try {
                JvmMonitorBCIAgent.agentmain(options, inst);
            } catch (Exception e) {
                e.printStackTrace();
                rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), "wrong options: "
                        + options);
                return new ResultPackage(cmd.getTaskId(), rInfo);
            }

            // just start profiling, instrumentation may not take effect at the moment.
            rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), TaskStatusInfo.SUCCESS_NULL.name(),
                    "start Instrumentation");
        } else if (action.equalsIgnoreCase("query")) {
            LOGGER.debug("process query: " + cmd);
            File file = new File(path);
            if (file.exists()) {
                String retMsg = readFileToString(path);
                if (retMsg.length() == 0) {
                    rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.NO_DATA.name(), "");
                } else {
                    rInfo = new ResultInfo(TaskStatus.COMPLETED.name(), "", retMsg);
                }
            } else {
                rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.NO_DATA.name(), "");
            }
        } else {
            LOGGER.error("Unreachable action: " + action);
            data = "TaskId: " + cmd.getTaskId() + " Unknown Flame graph action: " + action;
            LOGGER.error(data);
            rInfo = new ResultInfo(TaskStatus.ERROR.name(), TaskStatusInfo.MAL_FUNC.name(), data);
        }

        return new ResultPackage(cmd.getTaskId(), rInfo);
    }

    public static String processCommand(String cmdString, Instrumentation inst) {
        String ret = null;
        Command cmd = null;
        try {
            LOGGER.debug("Process Command: " + cmdString);
            cmd = mapper.readValue(cmdString, Command.class);
            // taskMap.put(cmd.getTaskID(), new TaskInfo(cmd, TaskStatus.BUSY));
        } catch (Exception e) {
            LOGGER.error("Illegal request command:" + cmdString + " (E): " + e);
            e.printStackTrace();
            // taskMap.put(cmd.getTaskID(), new TaskInfo(null, TaskStatus.ERROR));
            return null;
        }
        // TODO. record status?
        if (cmd == null) {
            LOGGER.error("Illegal request command:" + cmdString);
            return null;
        }

        ResultPackage result = processCommandInternal(cmd, inst);
        if (result.getResultInfo() == null) {
            LOGGER.error("Can not generate result package!");
            // updateTaskStatus(cmd.getTaskID(), TaskStatus.ERROR);
            return null;
        }

        try {
            ret = mapper.writeValueAsString(result);
            LOGGER.debug("resultPackage: " + ret);
            return ret;
        } catch (JsonProcessingException e) {
            LOGGER.error("Fail generate response json String");
            e.printStackTrace();
            return null;
        }
        // update status
        //updateTaskStatus(cmd.getTaskID(), TaskStatus.valueOf(result.getResultInfo().getStatus()));
    }

    class JTopThreadInfo {
        private boolean deltaDone;
        public long cpuTime;
        public ThreadInfo info;

        JTopThreadInfo(long cpuTime, ThreadInfo info) {
            this.cpuTime = cpuTime;
            this.info = info;
        }

        public void setDelta(long cpuTime, ThreadInfo info) {
             if (deltaDone) {
                 LOGGER.error("setDelta already called once");
                 return;
             }
             deltaDone = true;
             this.cpuTime = cpuTime - this.cpuTime;
             this.info = info;
        }
    }
}
