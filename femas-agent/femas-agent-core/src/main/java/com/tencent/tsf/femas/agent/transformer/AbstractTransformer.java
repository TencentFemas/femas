package com.tencent.tsf.femas.agent.transformer;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;

/**
 * Transform抽象父类，提供获取方法名的公用方法
 *
 * @Author leoziltong@tencent.com
 */
public abstract class AbstractTransformer implements ClassFileTransformer {
    protected static final byte[] EMPTY_BYTE_ARRAY = {};

    protected static String toClassName(final String classFile) {
        return classFile.replace('/', '.');
    }


    protected static String signatureOfMethod(final CtBehavior method) throws NotFoundException {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Modifier.toString(method.getModifiers()));
        if (method instanceof CtMethod) {
            final String returnType = ((CtMethod) method).getReturnType().getSimpleName();
            stringBuilder.append(" ").append(returnType);
        }
        stringBuilder.append(" ").append(method.getName()).append("(");

        final CtClass[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            if (i != 0) stringBuilder.append(", ");
            stringBuilder.append(parameterType.getSimpleName());
        }

        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
