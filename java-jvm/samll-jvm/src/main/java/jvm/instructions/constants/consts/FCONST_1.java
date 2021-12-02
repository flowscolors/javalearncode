package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class FCONST_1 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        frame.operandStack().pushFloat(1);
    }
}
