/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.tsf.femas.agent.transformer;

import com.tencent.tsf.femas.agent.transformer.async.transformer.AbstractTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.ExecutorTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.ForkJoinTransformer;
import com.tencent.tsf.femas.agent.transformer.async.transformer.TimerTaskTransformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2022/3/29 16:09
 */
public class CompositeTransformer extends AbstractTransformer {

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


    public CompositeTransformer(ExecutorTransformer executorTransformer, TimerTaskTransformer timerTaskTransformer, ForkJoinTransformer forkJoinTransformer) {
        this.executorTransformer = executorTransformer;
        this.timerTaskTransformer = timerTaskTransformer;
        this.forkJoinTransformer = forkJoinTransformer;
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classFile == null || classfileBuffer.length == 0 || loader == null) return EMPTY_BYTE_ARRAY;
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
