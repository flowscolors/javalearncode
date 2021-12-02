package jvm.instructions.conversions.d2x;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

public class D2I extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        double d = stack.popDouble();
        int i = (int) d;
        stack.pushInt(i);
    }

}
