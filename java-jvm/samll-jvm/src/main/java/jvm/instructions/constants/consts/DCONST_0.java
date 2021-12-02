package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

// push double
public class DCONST_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        frame.operandStack().pushDouble(0.0);
    }

}
