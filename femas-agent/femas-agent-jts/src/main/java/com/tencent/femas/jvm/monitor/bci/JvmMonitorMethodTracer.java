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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

// Class for method enter/exit time and invoke time.
public class JvmMonitorMethodTracer extends com.tencent.femas.jvm.monitor.bci.AbstractClassVisitor {
    private static final Logger LOGGER = Logger.getLogger(JvmMonitorMethodTracer.class);

    private boolean isInterface;

    public JvmMonitorMethodTracer(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isInterface = (access & ACC_INTERFACE) != 0;
        // LOGGER.debug("visit class name: " + name + " signature: " + signature + " isInterface: " + isInterface);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {

       // LOGGER.debug("visit method: " + name + " descriptor: " + descriptor + " signature: " + signature);
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (!isInterface && mv != null && !"<init>".equals(name) && !"<clinit>".equals(name)
                && isCandidateMethod(name)) {
            boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            // TODO: record native call?
            boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod && !isNativeMethod) {
                String loaderSig = calculateLoaderSig();
                String methodSig = name + descriptor;
                LOGGER.info("visitMethod: class: " + getClassName() + " method(desc): "
                        + methodSig + " loaderSig " + loaderSig);
                mv = new MethodTimerVisitor(api, mv, access, loaderSig, getClassName(), name, descriptor);
            }
        }
        return mv;
    }

    private static class MethodTimerVisitor extends MethodVisitor {

        private static final String METHOD_TRACE_RECORDER_WRAPPER_CLASS =
                "com/tencent/femas/jvm/monitor/bci/JvmMonitorMethodTraceRecorderWrapper";

        private static final String METHOD_TRACE_RECORDER_ON_METHOD_ENTER = "onMethodEnter";
        private static final String METHOD_TRACE_RECORDER_ON_METHOD_ENTER_DESC =
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
        private static final String METHOD_TRACE_RECORDER_ON_METHOD_EXIT = "onMethodExit";
        private static final String METHOD_TRACE_RECORDER_ON_METHOD_EXIT_DESC =
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V";

        private static final String METHOD_TRACE_RECORDER_BEFORE_METHOD_CALL = "beforeMethodCall";
        private static final String METHOD_TRACE_RECORDER_BEFORE_METHOD_CALL_DESC =
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";

        private static final String METHOD_TRACE_RECORDER_AFTER_METHOD_CALL = "afterMethodCall";
        private static final String METHOD_TRACE_RECORDER_AFTER_METHOD_CALL_DESC =
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH = "onArgumentPush";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_DESC = "(I)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_Z = "onArgumentPushBool";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_Z_DESC = "(IZZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_C = "onArgumentPushChar";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_C_DESC = "(ICZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_B = "onArgumentPushByte";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_B_DESC = "(IBZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_S = "onArgumentPushShort";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_S_DESC = "(ISZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_I = "onArgumentPushInt";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_I_DESC = "(IIZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_F = "onArgumentPushFloat";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_F_DESC = "(IFZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_J = "onArgumentPushLong";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_J_DESC = "(IJZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_D = "onArgumentPushDouble";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_D_DESC = "(IDZ)V";

        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_OBJ = "onArgumentPushObject";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_OBJ_DESC = "(ILjava/lang/Object;Z)V";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENTS_PUSH_FINISH_ALL = "onArgumentsPushFinishAll";
        private static final String METHOD_TRACE_RECORDER_ON_ARGUMENTS_PUSH_FINISH_ALL_DESC = "()V";


        private final int access;

        private final String loaderSig;
        private final String className;
        private final String methodName;
        private final String methodDesc;
        private final String methodSig;
        private final int argumentCount;
        private int line;

        public void visitLineNumber(int line, Label start) {
            this.line = line;
        }

        //methodSig = method name  +  method desc
        public MethodTimerVisitor(int api, MethodVisitor methodVisitor, int access,
                                  String loaderSig, String className, String methodName, String methodDesc) {
            super(api, methodVisitor);
            this.access = access;
            this.loaderSig = loaderSig;
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.methodSig = methodName + methodDesc;
            this.line = 0;

            Type methodType = Type.getMethodType(methodDesc);
            Type[] argsTypes = methodType.getArgumentTypes();
            this.argumentCount = argsTypes.length;
        }

        @Override
        public void visitCode() {
            LOGGER.debug("visitCode, method " + methodSig);
            super.visitCode();

            // pass arguments on stack.
            super.visitLdcInsn(this.className);
            super.visitLdcInsn(this.methodSig);
            super.visitLdcInsn(this.loaderSig);

            // TODO: explanation of why not calculate time in this method.
            super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                    METHOD_TRACE_RECORDER_ON_METHOD_ENTER, METHOD_TRACE_RECORDER_ON_METHOD_ENTER_DESC, false);

            // process arguments.
            boolean isStatic = ((access & ACC_STATIC) != 0);
            int idx = isStatic ? 0 : 1;

            Type methodType = Type.getMethodType(methodDesc);
            Type[] argsTypes = methodType.getArgumentTypes();
            int argc = 1;
            LOGGER.debug("Start pushing arugments .. ");
            for (Type tp : argsTypes) {
                int size = tp.getSize();
                pushArguments(this.className, this.methodSig, this.loaderSig, argc, tp, idx, true);
                argc++;
                idx += size;
            }

            super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                    METHOD_TRACE_RECORDER_ON_ARGUMENTS_PUSH_FINISH_ALL,
                    METHOD_TRACE_RECORDER_ON_ARGUMENTS_PUSH_FINISH_ALL_DESC, false);
            LOGGER.debug("Finish pushing arugments .. ");

            // TODO-zlin- dubug only, delete me
            /*
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            super.visitLdcInsn("after Method Enter..." + this.methodSig);
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            */
        }

        private void pushArguments(String className, String methodSig, String loaderSig,
                                   int argc, Type tp, int idx, boolean isArgument) {

            LOGGER.debug("generating code to push arguments, argc: " + argc + " tp: "
                    + tp.toString() + " idx: " + idx + " is Arg? " + isArgument);

            int sort = tp.getSort();
            if (sort == 0) {
                return;
            }
            LOGGER.debug("tp.sort is " + sort);

            int opcode = tp.getOpcode(ILOAD);


            // push arguments
            super.visitLdcInsn(argc);
            super.visitVarInsn(opcode, idx);
            super.visitLdcInsn(isArgument);
            switch (sort) {
                case Type.BOOLEAN:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_Z,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_Z_DESC, false);
                    return;
                case Type.CHAR:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_C,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_C_DESC, false);
                    return;
                case Type.BYTE:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_B,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_B_DESC, false);
                    return;
                case Type.SHORT:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_S,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_S_DESC, false);
                    return;
                case Type.INT:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_I,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_I_DESC, false);
                    return;
                case Type.FLOAT:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_F,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_F_DESC, false);
                    return;
                case Type.LONG:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_J,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_J_DESC, false);
                    return;
                case Type.DOUBLE:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_D,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_D_DESC, false);
                    return;
                case Type.ARRAY:
                case Type.OBJECT:
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_OBJ,
                            METHOD_TRACE_RECORDER_ON_ARGUMENT_PUSH_OBJ_DESC, false);
                    return;
                default:
                    LOGGER.warning("Bypass argument process of type " + sort);
                    // Delete me!
                    System.exit(-1);
            }
        }

        @Override
        public void visitInsn(int opcode) {
            boolean exitNormally = true;
            boolean generate = false;
            if ((opcode >= IRETURN && opcode <= RETURN)) {
                Type methodType = Type.getMethodType(methodDesc);
                Type returnType = methodType.getReturnType();
                int size = returnType.getSize();
                // String descriptor = returnType.getDescriptor();
                if (size == 1) {
                    super.visitInsn(DUP);
                } else if (size == 2) {
                    super.visitInsn(DUP2);
                } else {
                    assert opcode == RETURN;
                    // return void.
                }
                LOGGER.debug("Start pushing return value .. ");
                LOGGER.debug("Start pushing return value, the returntype is  " + returnType);
                pushArguments(this.className, this.methodSig, this.loaderSig,
                        0, returnType, 0, false);
                LOGGER.debug("Finish pushing return value .. ");
                generate = true;
            } else if (opcode == ATHROW) {
                exitNormally = false;
                generate = true;
            } else {
                generate = false;
            }
            if (generate) {
                generateMethodExitInsns(exitNormally);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // before invoke
            // pass arguments on stack.
            if (isInvokeOpcode(opcode)) {
                if (!owner.equalsIgnoreCase(METHOD_TRACE_RECORDER_WRAPPER_CLASS)) {
                    super.visitLdcInsn(owner);
                    super.visitLdcInsn(name);
                    super.visitLdcInsn(descriptor);
                    super.visitLdcInsn(this.line);
                    LOGGER.debug("before visit callee, opcode" + opcode + " method: " + owner + "." + name
                            + descriptor + " caller: " + methodSig);
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_BEFORE_METHOD_CALL,
                            METHOD_TRACE_RECORDER_BEFORE_METHOD_CALL_DESC, false);

                }

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                // after invoke
                if (!owner.equalsIgnoreCase(METHOD_TRACE_RECORDER_WRAPPER_CLASS)) {
                    super.visitLdcInsn(owner);
                    super.visitLdcInsn(name);
                    super.visitLdcInsn(descriptor);
                    super.visitLdcInsn(this.line);
                    LOGGER.debug("after visit callee, opcode" + opcode + " method: " + owner + "."
                            + name + descriptor + " caller: " + methodSig);
                    super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                            METHOD_TRACE_RECORDER_AFTER_METHOD_CALL,
                            METHOD_TRACE_RECORDER_AFTER_METHOD_CALL_DESC, false);
                }

            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }

        private boolean isInvokeOpcode(int opcode) {
            return opcode >= INVOKEVIRTUAL && opcode <= INVOKEINTERFACE;
        }

        private void generateMethodExitInsns(boolean normalReturn) {
            super.visitLdcInsn(this.className);
            super.visitLdcInsn(this.methodSig);
            super.visitLdcInsn(this.loaderSig);
            super.visitLdcInsn(normalReturn);

            LOGGER.debug("generate method exit insn: "  + this.methodSig + normalReturn);

            super.visitMethodInsn(INVOKESTATIC, METHOD_TRACE_RECORDER_WRAPPER_CLASS,
                    METHOD_TRACE_RECORDER_ON_METHOD_EXIT,
                    METHOD_TRACE_RECORDER_ON_METHOD_EXIT_DESC, false);
        }
    }
}
