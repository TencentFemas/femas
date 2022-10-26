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

import org.objectweb.asm.ClassVisitor;

import java.security.ProtectionDomain;
import java.util.Set;

public abstract class AbstractClassVisitor extends ClassVisitor implements IMethodVisitor {

    private ClassLoader loader;
    private String className;
    private Class<?> classBeingRedefined;
    private ProtectionDomain protectionDomain;
    private Set<String> candidateMethods;

    public AbstractClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void instantiate(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            Set<String> registeredMethods) {

        this.loader = loader;
        this.className = className;
        this.classBeingRedefined = classBeingRedefined;
        this.protectionDomain = protectionDomain;
        candidateMethods = registeredMethods;
    }

    public String calculateLoaderSig() {
        if (loader == null) {
            return "BootstrapLoader@null";
        }
        return loader.toString();
    }

    public Set<String> getCandidateMethods() {
        return candidateMethods;
    }

    public boolean isCandidateMethod(String m) {
        if (candidateMethods == null) {
            return false;
        }
        for (String s : candidateMethods) {
           if (s.equals(m)) {
               return true;
           }
        }
        return false;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public String getClassName() {
        return className;
    }

    public Class<?> getClassBeingRedefined() {
        return classBeingRedefined;
    }

    public ProtectionDomain getProtectionDomain() {
        return protectionDomain;
    }
}
