package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class LCONST_0  extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
         frame.operandStack().pushLong(0);
    }
}
