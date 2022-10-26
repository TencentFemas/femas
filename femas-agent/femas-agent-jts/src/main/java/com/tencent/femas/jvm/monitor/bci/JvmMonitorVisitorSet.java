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

import com.tencent.femas.jvm.monitor.utils.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.security.ProtectionDomain;
import java.util.Set;

public class JvmMonitorVisitorSet  {
    private com.tencent.femas.jvm.monitor.bci.AbstractClassVisitor topVisitor;
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorVisitorSet.class);

    public JvmMonitorVisitorSet(int api, ClassWriter classWriter,
                                ClassLoader loader, String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                Set<String> registeredMethods) {
        // TODO -  use configuration to create different visitor for different profiling scenario.

        com.tencent.femas.jvm.monitor.bci.AbstractClassVisitor methodTimer = new JvmMonitorMethodTracer(api, classWriter);

        // methodTimer must be the topmost visitor because we add record commit at method exit.
        topVisitor = methodTimer;
        assert (topVisitor instanceof JvmMonitorMethodTracer);
        topVisitor.instantiate(loader, className, classBeingRedefined, protectionDomain,registeredMethods);
    }

    public ClassVisitor getTopVisitor() {
        return topVisitor;
    }
}
