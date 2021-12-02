package jvm.instructions.comparisons.dcmp;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class DCMPG extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _dcmp(frame, true);
    }
    
}
