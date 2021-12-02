package jvm.instructions.references;

import jvm.instructions.base.InstructionIndex16;
import jvm.instructions.base.MethodInvokeLogic;
import jvm.rtda.Frame;
import jvm.rtda.heap.methodarea.MethodLookup;
import jvm.rtda.heap.methodarea.Object;
import jvm.rtda.heap.constantpool.MethodRef;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Method;

//invokespecial调用private方法和init方法
public class INVOKE_SPECIAL  extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        Class currentClass = frame.method().clazz();
        RunTimeConstantPool runTimeConstantPool = currentClass.constantPool();
        MethodRef methodRef = (MethodRef) runTimeConstantPool.getConstants(this.idx);
        Class resolvedClass = methodRef.resolvedClass();
        Method resolvedMethod = methodRef.ResolvedMethod();
        if ("<init>".equals(resolvedMethod.name()) && resolvedMethod.clazz() != resolvedClass) {
            throw new NoSuchMethodError();
        }
        if (resolvedMethod.isStatic()) {
            throw new IncompatibleClassChangeError();
        }

        Object ref = frame.operandStack().getRefFromTop(resolvedMethod.argSlotCount() - 1);
        if (null == ref) {
            throw new NullPointerException();
        }

        if (resolvedMethod.isProtected() &&
                resolvedMethod.clazz().isSubClassOf(currentClass) &&
                !resolvedMethod.clazz().getPackageName().equals(currentClass.getPackageName()) &&
                ref.clazz() != currentClass &&
                !ref.clazz().isSubClassOf(currentClass)) {
            throw new IllegalAccessError();
        }

        Method methodToBeInvoked = resolvedMethod;
        if (currentClass.isSuper() &&
                resolvedClass.isSubClassOf(currentClass) &&
                !resolvedMethod.name().equals("<init>")) {
            MethodLookup.lookupMethodInClass(currentClass.superClass, methodRef.name(), methodRef.descriptor());
        }

        if (methodToBeInvoked.isAbstract()) {
            throw new AbstractMethodError();
        }

        MethodInvokeLogic.invokeMethod(frame, methodToBeInvoked);

    }


}