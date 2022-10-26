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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

// TODO - add configuration for every method/class to choose different visitor
public class JvmMonitorTransformer implements ClassFileTransformer {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorTransformer.class);
    // <class, methodsList>
    private ConcurrentHashMap<String, Set<String>> registeredInfos;
    private TreeSet<Class<?>> registeredClasses;

    public TreeSet<Class<?>> getRegisteredClasses() {
        return registeredClasses;
    }

    public JvmMonitorTransformer() {
        registeredInfos = new ConcurrentHashMap<>();
        registeredClasses = new TreeSet<>(new com.tencent.femas.jvm.monitor.bci.JvmMonitorClassComparator());
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        ClassReader cr = new ClassReader(classfileBuffer);

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

        String clzName = classBeingRedefined.getName();
       // LOGGER.debug("JvmMonitorTransformer transform! enter class: " + clzName);
       // new Exception().printStackTrace(System.out);

        // TODO -  move to internal .  store original one.
        if ((!registeredInfos.isEmpty()) && registeredInfos.get(clzName) != null) {
            // LOGGER.debug("JvmMonitorTransformer transform! class: " + clzName);
            // make the change
            com.tencent.femas.jvm.monitor.bci.JvmMonitorVisitorSet visitorSet = new com.tencent.femas.jvm.monitor.bci.JvmMonitorVisitorSet(Opcodes.ASM7, cw,
                    loader, className,
                    classBeingRedefined,
                    protectionDomain,
                    registeredInfos.get(clzName));

            cr.accept(visitorSet.getTopVisitor(), 0);

            return cw.toByteArray();
        }
        return classfileBuffer;
    }

    private String getLoaderId(ClassLoader loader) {
        return loader == null ? "BootstrapLoader@null" : loader.toString();
    }

    public void clearRegisteredInfos() {
        registeredInfos.clear();
    }

    public void resetAllRegisteredClasses() {
        if (registeredInfos.isEmpty()) {
            return;
        }
        // empty registeredinfos, so no change applied.
        registeredInfos.clear();
    }

    public void postTransformProcess() {
        // for (Class clz : registeredClasses) {
        Class<?> clz = null;
        Iterator<Class<?>> iterator = registeredClasses.iterator();
        while (iterator.hasNext()) {
            clz = iterator.next();
            Set<String> methodList = registeredInfos.get(clz.getName());
            if (methodList == null || methodList.isEmpty()) {
                registeredInfos.remove(clz.getName());
                iterator.remove();
            }
         }
    }

    public void removeRegisteredMethod(String cname, Class clz, String mname) {
        Set<String> methodList = registeredInfos.get(cname);
        if (methodList == null) {
            LOGGER.error("removing from null method list in transformer");
        }
        if (!methodList.remove(mname)) {
            LOGGER.warn("removing unregistered method in transformer");
        }
        //  assert registeredClasses.contains(clz) :
        //  "unregister method must have classes registered! method: " + cname + "." +mname ;
    }

    public void registerClassAndMethod(String cname, Class clz, String mname) {
        Set<String> methodList = registeredInfos.get(cname);
        if (methodList == null) {
            methodList = new TreeSet<>();
            registeredInfos.put(cname, methodList);
            registeredClasses.add(clz);
        }
        methodList.add(mname);
    }
}
