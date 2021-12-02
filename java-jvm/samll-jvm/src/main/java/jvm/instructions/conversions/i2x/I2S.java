package jvm.instructions.conversions.i2x;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

//convert int to short
public class I2S extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int i = stack.popInt();
        short s = (short) i;
        stack.pushInt(s);
    }

}
