package com.tencent.tsf.femas.agent.transformer.async;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;


/**
 * @Author leoziltong@tencent.com
 */
public class ClassInfo {
    private final String className;
    private final byte[] classFileBuffer;
    private final ClassLoader loader;

    public ClassInfo(String className, byte[] classFileBuffer, ClassLoader loader) {
        this.className = className;
        this.classFileBuffer = classFileBuffer;
        this.loader = loader;
    }

    public String getClassName() {
        return className;
    }

    private CtClass ctClass;

    public CtClass getCtClass() throws IOException {
        if (ctClass != null) return ctClass;

        final ClassPool classPool = new ClassPool(true);
        if (loader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }

        final CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();

        this.ctClass = clazz;
        return clazz;
    }

    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public void setModified() {
        this.modified = true;
    }
}
