package jvm.instructions.references;

import jvm.instructions.base.InstructionIndex16;
import jvm.rtda.Frame;
import jvm.rtda.heap.constantpool.ClassRef;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Object;

public class NEW extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        RunTimeConstantPool cp = frame.method().clazz().constantPool();
        ClassRef classRef = (ClassRef) cp.getConstants(this.idx);
        Class clazz = classRef.resolvedClass();
        if (clazz.isInterface() || clazz.isAbstract()) {
            throw new InstantiationError();
        }
        Object ref = clazz.newObject();
        frame.operandStack().pushRef(ref);
    }

}