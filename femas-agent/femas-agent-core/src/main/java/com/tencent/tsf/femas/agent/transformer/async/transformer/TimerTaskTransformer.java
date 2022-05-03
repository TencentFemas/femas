package com.tencent.tsf.femas.agent.transformer.async.transformer;

import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.agent.transformer.async.ClassInfo;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;


/**
 * @Author leoziltong@tencent.com
 */
public class TimerTaskTransformer extends AbstractTransformer {

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<>();
    private static final Set<String> INTERCEPT_METHEDS = new HashSet<>();
    private static final String TIMERTASK_CLASS_NAME = "java.util.TimerTask";

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.Timer");

        INTERCEPT_METHEDS.add("run");
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        return new byte[0];
        if (classFile == null || classfileBuffer.length == 0) return EMPTY_BYTE_ARRAY;
        final String className = toClassName(classFile);
        try {
            if (EXECUTOR_CLASS_NAMES.contains(className)) {
                ClassInfo classInfo = new ClassInfo(className, classfileBuffer, loader);
                for (CtMethod method : classInfo.getCtClass().getDeclaredMethods()) {
                    String code = "";
                    CtClass[] parameterTypes = method.getParameterTypes();
                    int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1;

                    for (int i = 0; i < parameterTypes.length; i++) {
                        final String paramTypeName = parameterTypes[i].getName();
                        if (TIMERTASK_CLASS_NAME.equals(paramTypeName)) {
                            code += String.format("$%d = com.tencent.tsf.femas.agent.transformer.async.RunnableWrapper.wrapTimerTask($%d);", i + pos, i + pos);
                        }
                    }

                    if (code.length() > 0) {
                        method.insertBefore(code);
//                        AgentLogger.getLogger().severe("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ": " + code);
                    }
                }
                classInfo.getCtClass().detach();
                return classInfo.getCtClass().toBytecode();
            }
        } catch (IOException e) {
            AgentLogger.getLogger().severe("ExecutorTransformer.transform error: " + AgentLogger.getStackTraceString(e));
        } catch (NotFoundException e) {
            AgentLogger.getLogger().severe("ExecutorTransformer.transform error: " + AgentLogger.getStackTraceString(e));
        } catch (CannotCompileException e) {
            AgentLogger.getLogger().severe("ExecutorTransformer.transform error: " + AgentLogger.getStackTraceString(e));
        }

        return EMPTY_BYTE_ARRAY;
    }
}
