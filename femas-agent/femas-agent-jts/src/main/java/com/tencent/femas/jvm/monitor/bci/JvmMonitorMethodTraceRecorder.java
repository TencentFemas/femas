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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tencent.femas.jvm.monitor.jvmmonitoragent.JvmMonitorAgentEntrance;
import com.tencent.femas.jvm.monitor.utils.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;


// TO-DO: ZLIN - CREATE a wrapper class and invoke with loader.
public class JvmMonitorMethodTraceRecorder {
    private static final Logger LOGGER = Logger.getLogger(MethodTraceRecord.class);
    // TODO: try to use concurrentHashMap?  may not satisfy check-update scenario.

    /* <methodSig, Record> */
    private static ThreadLocal<ArrayList<MethodTraceRecord>> methodRecordStack =
            new ThreadLocal<ArrayList<MethodTraceRecord>>();

    private static ThreadLocal<ArrayList<MethodArgumentRecord>> methodArgumentList =
            new ThreadLocal<ArrayList<MethodArgumentRecord>>();

    private static ThreadLocal<Object> mRetValue = new ThreadLocal<>();
    // private static HashMap<String, MethodTraceRecord> methodTable;

    public static void removeProfileCount(String getKey, HashMap<String, Integer> map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String keyPattern = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getPatternFromString(key);
            if (Pattern.matches(keyPattern, getKey)) {
                map.remove(key);
            }
        }
    }

    public static int getProfileCount(String getKey, HashMap<String, Integer> map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String keyPattern = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getPatternFromString(key);
            if (Pattern.matches(keyPattern, getKey)) {
                return (int) entry.getValue();
            }
        }
        return -1;
    }

    public static void setProfileCount(String getKey, HashMap<String, Integer> map, int profileCount) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String keyPattern = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getPatternFromString(key);
            if (Pattern.matches(keyPattern, getKey)) {
                map.put(key, profileCount);
            }
        }
    }

    public static void onMethodEnter(String className, String methodSig, String loaderSig) {
        synchronized (JvmMonitorMethodTraceRecorder.class) {

            String mtdName = methodSig.substring(0, methodSig.indexOf('('));
            String getKey = className + "." + mtdName;
            int profileCount = getProfileCount(getKey, JvmMonitorAgentEntrance.countTable);

            if (profileCount > 1) {
                setProfileCount(getKey, JvmMonitorAgentEntrance.countTable, --profileCount);
            } else if (profileCount == 1) {
                JvmMonitorTransformer transformer = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getJvmMonitorTransformer();
                Instrumentation inst = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getInstrumentation();
                String clzPattern = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getPatternFromString(className.replace("/","."));
                String methodPattern = com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.getPatternFromString(mtdName);

                for (Class clz : transformer.getRegisteredClasses()) {
                    // name used in forName()
                    String cname = clz.getName();
                    // this is not possible. delete me
                    if (com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.shouldExcludeClass(cname)) {
                        // LOGGER.debug("Exclude class {" + cname + "} for instrument");
                        continue;
                    }
                    if (Pattern.matches(clzPattern, cname)) {
                        Method[] clzMethods = clz.getDeclaredMethods();
                        for (Method mtd : clzMethods) {
                            if (Pattern.matches(clzPattern, cname) && Pattern.matches(methodPattern, mtd.getName())) {
                                LOGGER.debug("filter out method {" + clz.getName() + "." + mtd.getName()
                                        + "} that in restore list");
                                transformer.removeRegisteredMethod(cname, clz, mtd.getName());
                            }
                        }
                    }
                }

                try {
                    TreeSet<Class<?>> classesSet = transformer.getRegisteredClasses();
                    inst.addTransformer(transformer, true);
                    Class<?>[] classesArray = new Class<?>[classesSet.size()];
                    classesArray = classesSet.toArray(classesArray);
                    LOGGER.debug("start to retransform classes count: " + classesArray.length);
                    inst.retransformClasses(classesArray);
                    transformer.postTransformProcess();
                    inst.removeTransformer(transformer); }
                catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                }

                setProfileCount(getKey, JvmMonitorAgentEntrance.countTable, --profileCount);
            }

            ArrayList<MethodTraceRecord> recordStack = methodRecordStack.get();
          //  LOGGER.debug("get record stack: " + recordStack);

            if (recordStack == null) {
                recordStack = new ArrayList<>();
                methodRecordStack.set(recordStack);
            }
           // LOGGER.debug("after newrecord stack: " + recordStack);

            MethodTraceRecord topRecord = null;
            if (!recordStack.isEmpty()) {
                int size = recordStack.size();
                topRecord = recordStack.get(size - 1);
            };
           // LOGGER.debug("get record form thread local: " + topRecord);

            int layer = 0;
            if (topRecord != null) {
                layer = topRecord.layer + 1;
            }
            long time = System.currentTimeMillis();
           // LOGGER.debug("get record form thread local: " + topRecord);
            MethodTraceRecord record = new MethodTraceRecord(className, methodSig, loaderSig, time, layer);
            methodRecordStack.get().add(record);

           // LOGGER.debug("finish create record when method { " + methodSig + " } enter");
        }
    }


    public static void onMethodExit(String className, String methodSig, String loaderSig, boolean returnNormally) {
        long time;
        time = System.currentTimeMillis();

        assert methodRecordStack.get() != null : "methodRecordStack is null after method call";
        assert !methodRecordStack.get().isEmpty()  : "methodRecordStack is empty after method call";
        //LOGGER.debug("finish record when method { " + methodSig + " } exit");

        ArrayList<MethodTraceRecord> recordStack = methodRecordStack.get();
        // remove the top most record as method exits.
        MethodTraceRecord record = recordStack.remove(recordStack.size() - 1);

        ArrayList<MethodArgumentRecord> argumentRecordList = methodArgumentList.get();
        if (record == null || record.execBeginTime == 0) {
            LOGGER.error("No method enter record!");
            // zlin: delete me!!
            System.exit(-1);
            return;
        }
        record.threadName = Thread.currentThread().toString();
        record.execEndTime = time;
        record.execDuration = time - record.execBeginTime;
        // Do not update argList because it can be rewrite by sub methods.
        // record.argsList = argumentRecordList;
        record.returnNormally = returnNormally;
        if (mRetValue.get() != null) {
            record.returnValue = mRetValue.get().getClass().getName();
        } else {
            record.returnValue = null;
        }

        //LOGGER.debug("commit record when method { " + record.methodSig + " } exit");
        // All method info are collected successfully.  merge it to global one.
        commitRecord(record);
        // clear arguments and return value.
        assert methodArgumentList.get() == null : "argument list is not clean when method exit! data lose??";
        mRetValue.remove();
    }

    public static void beforeMethodCall(String owner, String name, String descriptor, int line) {
        assert methodRecordStack.get() != null : "methodRecordStack is null before method call";
        assert !methodRecordStack.get().isEmpty()  : "methodRecordStack is empty before method call";
        ArrayList<MethodTraceRecord> recordStack = methodRecordStack.get();
        MethodTraceRecord record = recordStack.get(recordStack.size() - 1);
        //LOGGER.debug("insert before method call { " + name + " } at line: " + line);
        if (record == null || record.execBeginTime == 0) {
            LOGGER.error("No method enter record!");
            // zlin: delete me!!
            System.exit(-1);
            return;
        }

        record.insertCallee(owner, name, descriptor, line);
    }

    public static void afterMethodCall(String owner, String name, String descriptor, int line) {
        long time;
        time = System.currentTimeMillis();
        assert methodRecordStack.get() != null : "methodRecordStack is null after method call";
        assert !methodRecordStack.get().isEmpty()  : "methodRecordStack is empty after method call";

        ArrayList<MethodTraceRecord> recordStack = methodRecordStack.get();
        MethodTraceRecord record = recordStack.get(recordStack.size() - 1);

        //LOGGER.debug("insert after method call { " + name + " } at line: " + line);
        if (record == null || record.execBeginTime == 0) {
            LOGGER.error("No method enter record!");
            // zlin: delete me!!
            System.exit(-1);
            return;
        }
        record.updateCallee(owner, name, descriptor, line, time);
    }

    public static void onArgumentPushBool(int idx, boolean arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushChar(int idx, char arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushByte(int idx, byte arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushShort(int idx, short arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushInt(int idx, int arg, boolean isArgument) {
        // LOGGER.debug("push int " + arg + "isArg? : " + isArgument);
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushFloat(int idx, float arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushDouble(int idx, double arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushLong(int idx, long arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentPushObject(int idx, Object arg, boolean isArgument) {
        processArgumentsAndRets(idx, arg, isArgument);
    }

    public static void onArgumentsPushFinishAll() {
        if (methodArgumentList.get() == null || methodArgumentList.get().isEmpty()) {
            return;
        }

        assert methodRecordStack.get() != null : "methodRecordStack is null after method call";
        assert !methodRecordStack.get().isEmpty()  : "methodRecordStack is empty after method call";

        ArrayList<MethodTraceRecord> recordStack = methodRecordStack.get();
        MethodTraceRecord record = recordStack.get(recordStack.size() - 1);
        if (record.argsList != null) {
            LOGGER.error("error when updating argList, it is not NULL: " + record.argsList);
        }
        record.argsList = methodArgumentList.get();
        methodArgumentList.remove();
    }

    // process pushed record. the record in this method must be the one contains all profiling info of a invoked method.
    private static synchronized  void commitRecord(MethodTraceRecord record) {
        JvmMonitorRecordProcessor.commit(record);
    }

    private static void processArgumentsAndRets(int idx, Object arg, boolean isArgument) {
        if (!isArgument) {
            // save return val
            // LOGGER.debug("processArgumentsAndRets mRetValue: " + mRetValue.get());
            assert mRetValue.get() == null : "double return?";
            mRetValue.set(arg);
            return;
        }

        //LOGGER.debug("processArgumentsAndRets, methodArgumentList: " + methodArgumentList.get());
        assert ((methodArgumentList.get() == null) || (methodArgumentList.get().size() == (idx - 1)))
                : "Wrong argument recorded, expected: " + (idx - 1) + " but actual " + methodArgumentList.get().size();
        MethodArgumentRecord record = new MethodArgumentRecord(idx, arg);
        if (methodArgumentList.get() == null) {
            methodArgumentList.set(new ArrayList<>());
        }
        methodArgumentList.get().add(record);
    }

    /*
        A record for method trace.
     */
    static class MethodTraceRecord {
        public ArrayList<MethodArgumentRecord> argsList;
        public boolean returnNormally;
        public String returnValue;
        private String threadName;          // thread identifier
        private int layer;                  // function call layer
        private String className;
        private String methodSig;
        private String methodName;
        private String loaderName;
        private long execBeginTime;
        private long execEndTime;
        private long execDuration;
        private long profileCountLeft;

        /* calleeSig, record */
        private LinkedList<InvocationTargetRecord> calleeRecordList;

        @JsonIgnore
        private InvocationTargetRecord currentInvocationRecord;

        public MethodTraceRecord(String className, String methodSig, String loaderName, long enterTime, int layer) {
            this.className = className.replace("/", ".");
            this.methodSig = methodSig;
            this.loaderName = loaderName;
            this.execBeginTime = enterTime;
            this.layer = layer;
            this.argsList = null;

            String mtdName = methodSig.substring(0, methodSig.indexOf('('));
            this.methodName = mtdName;
            String getKey = className + "." + mtdName;
            int count = getProfileCount(getKey, JvmMonitorAgentEntrance.countTable);
            this.profileCountLeft = count;

            // If count equals 0, remove this key
            if (count == 0) {
                removeProfileCount(getKey, JvmMonitorAgentEntrance.countTable);
            }
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        public int getLayer() {
            return layer;
        }

        public void setLayer(int layer) {
            this.layer = layer;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodSig() {
            return methodSig;
        }

        public void setMethodSig(String methodSig) {
            this.methodSig = methodSig;
        }

        public String getLoaderName() {
            return loaderName;
        }

        public void setLoaderName(String loaderName) {
            this.loaderName = loaderName;
        }

        public long getExecBeginTime() {
            return execBeginTime;
        }

        public void setExecBeginTime(long execBeginTime) {
            this.execBeginTime = execBeginTime;
        }

        public long getExecEndTime() {
            return execEndTime;
        }

        public void setExecEndTime(long execEndTime)  {
            this.execEndTime = execEndTime;
        }

        public long getExecDuration() {
            return execDuration;
        }

        public void setExecDuration(long execDuration) {
            this.execDuration = execDuration;
        }

        public LinkedList<InvocationTargetRecord> getCalleeRecordList() {
            return calleeRecordList;
        }

        public void setCalleeRecordList(LinkedList<InvocationTargetRecord> calleeRecordList) {
            this.calleeRecordList = calleeRecordList;
        }

        public void insertCallee(String owner, String name, String descriptor, int line) {
            if (calleeRecordList == null) {
                calleeRecordList = new LinkedList<>();
            }

            // TODO - zlin: add line number to handle aba case.
            currentInvocationRecord = getRecordInList(owner, name, descriptor, line, true);
            assert currentInvocationRecord != null :
                    "getRecordInList() returns null, which is not possible for createIfNotExist. OOM may happened!";
            currentInvocationRecord.execBeginTime = System.currentTimeMillis();
            currentInvocationRecord.line = line;
        }

        private InvocationTargetRecord getRecordInList(String owner, String name, String descriptor, int line,
                                                       boolean createIfNotExist) {
            String sig = getCalleeSig(name,descriptor,line);
            for (InvocationTargetRecord r : calleeRecordList) {
                if (r.calleeSig.equals(sig)) {
                    return r;
                }
            }
            if (createIfNotExist) {
                InvocationTargetRecord r = new InvocationTargetRecord(owner, name, sig);
                calleeRecordList.add(r);
                return r;
            }
            return null;
        }

        private String getCalleeSig(String name, String descriptor, int line) {
            return name + descriptor + "#" + line;
        }

        public void updateCallee(String owner, String name, String descriptor, int line, long time) {
            String calleeSig = getCalleeSig(name, descriptor, line);
            InvocationTargetRecord record = currentInvocationRecord;
            if (!record.calleeSig.equals(calleeSig)) {
                // delete me !!
                LOGGER.error("wrong invocation method signature, expected: " + calleeSig + " actual in record: "
                        + record.calleeSig);
                System.exit(-1);
            }

            if (record.execBeginTime == 0) {
                // delete me !!
                LOGGER.error("wrong invocation method execBeginTime: 0");
                System.exit(1);
            }
            record.execEndTime = time;
            record.execDuration = record.execEndTime - record.execBeginTime;
            record.invocationCount++;
        }
    }

    private static class InvocationTargetRecord {
        public int line;
        private String calleeSig;
        private String methodName;
        private String className;
        private long invocationCount;
        private long execBeginTime;
        private long execEndTime;
        private long execDuration;
        private int shouldNotDeepin;

        // TODO - arguments and return

        public InvocationTargetRecord(String owner, String name, String sig) {
            this.calleeSig = sig;
            this.className = owner.replace("/", ".");
            this.execBeginTime = 0;
            this.methodName = name;
            if (com.tencent.femas.jvm.monitor.bci.JvmMonitorBCIAgent.shouldExcludeClass(owner.replace("/", "."))) {
                shouldNotDeepin = 1;
            } else {
                shouldNotDeepin = 0;
            }
        }

    }

    private static class MethodArgumentRecord {
        private int index;
        private Object value;
        // private String name;

        public MethodArgumentRecord(int index, Object value) {
            this.index = index;
            this.value = value;
        }
    }

}
