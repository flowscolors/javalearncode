package jvm.instructions.conversions.i2x;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

//Convert int to byte
public class I2B extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int i = stack.popInt();
        int b = (byte) i;
        stack.pushInt(b);
    }

}
