package jvm.instructions.constants.ldc;

import jvm.instructions.base.InstructionIndex16;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;

public class LDC_W extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        _ldc(frame, this.idx);
    }

    private void _ldc(Frame frame, int idx) {
        OperandStack stack = frame.operandStack();
        RunTimeConstantPool runTimeConstantPool = frame.method().clazz().constantPool();
        Object c = runTimeConstantPool.getConstants(idx);

        if (c instanceof Integer) {
            stack.pushInt((Integer) c);
            return;
        }

        if (c instanceof Float) {
            stack.pushFloat((Float) c);
            return;
        }

        throw new RuntimeException("todo ldc");
    }

}
