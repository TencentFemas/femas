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

package com.tencent.femas.jvm.monitor.utils;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpSocketServer implements RpcServer {
    private static final Logger LOGGER = Logger.getLogger(HttpSocketServer.class);
    private static volatile HttpSocketServer instance;
    private static HttpHandler handler;
    private static AtomicBoolean stopped;
    private HttpServer httpServer;
    private String port;
    private static final int PARALLEL_THREAD_NUMBER = 4;

    public static HttpSocketServer getInstance(String port, HttpHandler handler) {
        if (instance == null) {
            synchronized (HttpSocketServer.class) {
                if (instance == null) {
                    instance = new HttpSocketServer(port, handler);
                }
            }
        }

        // Handler and port update. TODO: don't use singleton!
        if (!instance.port.equalsIgnoreCase(port)) {
            instance.port = port;
        }
        if (!instance.handler.equals(handler)) {
            instance.handler = handler;
        }
        return instance;
    }

    private HttpSocketServer(String port, HttpHandler handler) {
        this.port = port;
        this.handler = handler;
        stopped = new AtomicBoolean(true);
    }

    public void start(String url) throws IOException {
        assert handler != null;
        // Can only start when stopped.
        assert stopped.get() != false;
        LOGGER.debug("Starting listen on http url: http://localhost:" + port + url);
        httpServer = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);

        HttpContext context = httpServer.createContext(url, handler);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopHook();
            }
        });

        final ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                final Thread thread = new Thread(runnable, "TencentCloudJvmMonitor-cmd-processor");
                thread.setDaemon(true);
                thread.setContextClassLoader(this.getClass().getClassLoader());
                return thread;
            }
        };
        httpServer.setExecutor(Executors.newFixedThreadPool(PARALLEL_THREAD_NUMBER, threadFactory));
        httpServer.start();
    }

    private void stop() {
        if (stopped.get() == true) {
            return;
        }
        stopped.set(true);
        httpServer.stop(2);
    }

    private static void stopHook() {
        LOGGER.debug("Stop httpSocketServer in ShutdownHook");
        if (instance != null) {
            instance.stop();
        }
    }
}
