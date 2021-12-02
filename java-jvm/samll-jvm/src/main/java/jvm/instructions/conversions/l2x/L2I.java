package jvm.instructions.conversions.l2x;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

//convert long to int
public class L2I extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        long l = stack.popLong();
        int i = (int) l;
        stack.pushInt(i);
    }
}
