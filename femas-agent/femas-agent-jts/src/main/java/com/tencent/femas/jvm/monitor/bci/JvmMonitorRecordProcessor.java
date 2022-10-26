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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.femas.jvm.monitor.jvmmonitoragent.HotspotCommandProcessor;
import com.tencent.femas.jvm.monitor.utils.JvmMonitorUtils;
import com.tencent.femas.jvm.monitor.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;


public class JvmMonitorRecordProcessor {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorRecordProcessor.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ReentrantLock lock = new ReentrantLock();
    private static boolean initialized = false;
    private static final int INTERNAL_BUFFER_LENGTH = 8 * 1024;

    public static FileOutputStream fos;
    public static BufferedOutputStream bos;

    static {
        initialize();
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        initialized = true;
        // dumped data would be flush when topmost methods exits.
        // close before jvm shutdown.
        Thread recordProcessorHook = new Thread() {
            public void run() {
                JvmMonitorRecordProcessor.close();
            }
        };
        Runtime.getRuntime().addShutdownHook(recordProcessorHook);
    }

    public static void commit(com.tencent.femas.jvm.monitor.bci.JvmMonitorMethodTraceRecorder.MethodTraceRecord record) {
        commitMethodTraceRecord(record);
        if (record.getLayer() == 0) {
            // top most method.
            flush();
        }
    }

    private static void commitMethodTraceRecord(com.tencent.femas.jvm.monitor.bci.JvmMonitorMethodTraceRecorder.MethodTraceRecord record) {
        try {
            String sRecord = mapper.writeValueAsString(record);
            LOGGER.debug("Method Record in json: " + sRecord);
            try {
                String path = JvmMonitorUtils.getDataSavePath() + HotspotCommandProcessor.METHOD_TRACE_DUMP_FILE;
                LOGGER.debug("profiler dump file: " + path);
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                fos = new FileOutputStream(file, true);
                bos = new BufferedOutputStream(fos, INTERNAL_BUFFER_LENGTH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lock.lock();
            bos.write(sRecord.getBytes());
            bos.write('\n');
            lock.unlock();
        } catch (IOException e) {
            LOGGER.error("Fail generate method trace record String");
            e.printStackTrace();
        }
    }

    public static void flush() {
        try {
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            bos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
