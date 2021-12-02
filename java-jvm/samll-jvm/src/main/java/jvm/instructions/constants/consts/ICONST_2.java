package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class ICONST_2 extends InstructionNoOperands {
    @Override
    public void execute(Frame frame) {
        frame.operandStack().pushInt(2);
    }
}
