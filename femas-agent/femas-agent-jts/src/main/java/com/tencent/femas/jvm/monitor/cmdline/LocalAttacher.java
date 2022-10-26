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

package com.tencent.femas.jvm.monitor.cmdline;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.tencent.femas.jvm.monitor.utils.Logger;

import java.io.IOException;
import java.util.Properties;

public class LocalAttacher {
    private static final Logger LOGGER = Logger.getLogger(LocalAttacher.class);

    public VirtualMachine attach(String pid) {
        if (vm == null) {
            try {
                vm = VirtualMachine.attach(pid);
            } catch (AttachNotSupportedException e) {
                e.printStackTrace();
                LOGGER.error("The virtual machine does not support attach!");
                return null;
            } catch (IOException e) {
                LOGGER.error("IO error when attach to virtual machine!");
                e.printStackTrace();
                return null;
            }
        } else {
            LOGGER.error("already attached to virtual machine: " + vm.id());
            // return as it is already checked the system preperty at first attach.
            return vm;
        }
        // Version check.
        if (vm != null) {
            Properties targetProp;
            try {
                targetProp = vm.getSystemProperties();
            } catch (IOException e) {
                LOGGER.warn("Can not get JDK properties, skip!");
                e.printStackTrace();
                return vm;
            }
            String targetJavaVersion = targetProp.getProperty("java.specification.version");
            String selfJavaVersion = System.getProperty("java.specification.version");
            if (targetJavaVersion != null && selfJavaVersion != null && !targetJavaVersion.equals(selfJavaVersion)) {
                LOGGER.warn("TencentCloudJvmMonitor works with different JDK, data maybe inaccurate,"
                        + " please use TencentKona JDK");
            }
        }
        return vm;
    }

    public void detach() {
        if (vm != null) {
            try {
                vm.detach();
            } catch (IOException e) {
                e.printStackTrace();
            }
            vm = null;
        }
    }

    private VirtualMachine vm;
}
