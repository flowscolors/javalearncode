package jvm.instructions.comparisons.fcmp;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class FCMPL extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _fcmp(frame, false);
    }

}
