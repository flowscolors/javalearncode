package jvm.instructions.references;

import jvm.instructions.base.InstructionIndex16;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.constantpool.ClassRef;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Object;

public class CHECK_CAST extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        Object ref = stack.popRef();
        stack.pushRef(ref);
        if (null == ref) return;
        RunTimeConstantPool cp = frame.method().clazz().constantPool();
        ClassRef clazzRef = (ClassRef) cp.getConstants(this.idx);
        Class clazz = clazzRef.resolvedClass();
    }

}
