package jvm.instructions.constants.nop;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class NOP extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        //really do nothing
    }

}
