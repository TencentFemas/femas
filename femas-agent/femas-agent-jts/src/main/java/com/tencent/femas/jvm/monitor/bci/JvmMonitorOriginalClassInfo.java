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

import java.security.ProtectionDomain;

public class JvmMonitorOriginalClassInfo {
    private ClassLoader loader;
    private String className;
    ProtectionDomain protectionDomain;
    private byte[] originalClassFileBuffer;
    private int version;

    public JvmMonitorOriginalClassInfo(ClassLoader loader, String className,
                                       ProtectionDomain domain, byte[] originalClassFileBuffer, int version) {
        this.loader = loader;
        this.className = className;
        this.protectionDomain = domain;
        this.originalClassFileBuffer = originalClassFileBuffer;
        this.version = version;
    }

    public boolean sameWith(JvmMonitorOriginalClassInfo originalInfo) {
        return (this.loader.equals(originalInfo.loader))
                && (this.className.equals(originalInfo.className));
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public String getClassName() {
        return className;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }

    public byte[] getOriginalClassFileBuffer() {
        return originalClassFileBuffer;
    }

    public int getVersion() {
        return version;
    }
}
