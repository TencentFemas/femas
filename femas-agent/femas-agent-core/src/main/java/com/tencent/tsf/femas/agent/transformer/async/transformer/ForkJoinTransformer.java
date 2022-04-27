package com.tencent.tsf.femas.agent.transformer.async.transformer;

import com.tencent.tsf.femas.agent.tools.AgentLogger;
import com.tencent.tsf.femas.agent.transformer.async.ClassInfo;
import javassist.CannotCompileException;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author leoziltong@tencent.com
 */
public class ForkJoinTransformer extends AbstractTransformer {

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<>();

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ForkJoinTask");
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//        logger.info("add new field " + capturedFieldName + " to class " + className);
        if (classFile == null || classfileBuffer.length == 0) return EMPTY_BYTE_ARRAY;
        final String className = toClassName(classFile);
        try {
            if (EXECUTOR_CLASS_NAMES.contains(className)) {
                ClassInfo classInfo = new ClassInfo(className, classfileBuffer, loader);
                final String capturedFieldName = "tag";
                final CtField capturedField = CtField.make("private final String " + capturedFieldName + ";", classInfo.getCtClass());
                classInfo.getCtClass().addField(capturedField, "com.tencent.tsf.femas.agent.transformer.async.AgentContext.getAgentHead();");
                CtMethod ctMethod = classInfo.getCtClass().getDeclaredMethod("doExec");
                String beforeCode = "";
                String afterCode = "";
                beforeCode += String.format("com.tencent.tsf.femas.agent.transformer.async.AgentContext.setAgentHead(tag);");
                afterCode += String.format("com.tencent.tsf.femas.agent.transformer.async.AgentContext.clearContext();");
                if (beforeCode.length() > 0) {
                    ctMethod.insertBefore(beforeCode);
//                    AgentLogger.getLogger().severe("insert code before method " + signatureOfMethod(ctMethod) + " of class " + ctMethod.getDeclaringClass().getName() + ": " + beforeCode);
                }
                if (afterCode.length() > 0) {
                    ctMethod.insertAfter(afterCode);
//                    AgentLogger.getLogger().severe("insert code after method " + signatureOfMethod(ctMethod) + " of class " + ctMethod.getDeclaringClass().getName() + ": " + afterCode);
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
