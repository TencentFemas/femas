package com.tencent.tsf.femas.agent.transformer.async.transformer;


import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * Transformer：实现跨线程功能，在通过指定的方法提交线程时修改当前线程对应的ThreadLocal，并在线程结束后还原
 *
 * @Author leoziltong@tencent.com
 */
public class ThreadPoolTransformer extends AbstractTransformer {

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<>();
    private static Set<String> TIME_CLASS_NAMES = new HashSet<>();
    private static Set<String> FORKJOIN_CLASS_NAMES = new HashSet<>();

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ThreadPoolExecutor");
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ScheduledThreadPoolExecutor");
        TIME_CLASS_NAMES.add("java.util.Timer");
        FORKJOIN_CLASS_NAMES.add("java.util.concurrent.ForkJoinTask");
    }

    private ExecutorTransformer executorTransformer;
    private TimerTaskTransformer timerTaskTransformer;
    private ForkJoinTransformer forkJoinTransformer;


    public ThreadPoolTransformer(ExecutorTransformer executorTransformer, TimerTaskTransformer timerTaskTransformer, ForkJoinTransformer forkJoinTransformer) {
        this.executorTransformer = executorTransformer;
        this.timerTaskTransformer = timerTaskTransformer;
        this.forkJoinTransformer = forkJoinTransformer;
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        if(CrossClassLoaderCache.getSwitch())
//            return EMPTY_BYTE_ARRAY;
        if (classFile == null || classfileBuffer.length == 0) return EMPTY_BYTE_ARRAY;
        final String className = toClassName(classFile);
        if (EXECUTOR_CLASS_NAMES.contains(className)) {
            return executorTransformer.transform(loader, classFile, classBeingRedefined, protectionDomain, classfileBuffer);
        } else if (FORKJOIN_CLASS_NAMES.contains(className)) {
            return forkJoinTransformer.transform(loader, classFile, classBeingRedefined, protectionDomain, classfileBuffer);
        } else if (TIME_CLASS_NAMES.contains(className)) {
            return timerTaskTransformer.transform(loader, classFile, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        return EMPTY_BYTE_ARRAY;
    }
}
