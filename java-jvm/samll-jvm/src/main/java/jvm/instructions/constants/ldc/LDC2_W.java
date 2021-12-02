package jvm.instructions.constants.ldc;

import jvm.instructions.base.InstructionIndex16;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;


public class LDC2_W extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        RunTimeConstantPool runTimeConstantPool = frame.method().clazz().constantPool();
        Object c = runTimeConstantPool.getConstants(this.idx);
        if (c instanceof Long) {
            stack.pushLong((Long) c);
            return;
        }
        if (c instanceof Double){
            stack.pushDouble((Double) c);
        }
        throw new ClassFormatError();

    }

}
