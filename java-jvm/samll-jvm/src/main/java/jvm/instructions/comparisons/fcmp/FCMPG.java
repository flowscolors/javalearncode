package jvm.instructions.comparisons.fcmp;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class FCMPG extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _fcmp(frame, true);
    }

}
