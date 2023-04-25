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

import com.tencent.femas.tencentcloudjvmmonitor.utils.JvmMonitorHttpRequestHandler;
import com.tencent.femas.tencentcloudjvmmonitor.utils.Logger;

import java.lang.instrument.Instrumentation;


public final class HotspotRequestHandler extends JvmMonitorHttpRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(HotspotRequestHandler.class);
    private static final int BUFFER_SIZE = 1024;
    private Instrumentation inst;

    public HotspotRequestHandler(Instrumentation inst) {
        this.inst = inst;
    }

    public String processCommand(String requestBody) {
        return HotspotCommandProcessor.processCommand(requestBody, inst);
    }
}