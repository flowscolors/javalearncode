package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

//push null
public class ACONST_NULL extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        frame.operandStack().pushRef(null);
    }

}
