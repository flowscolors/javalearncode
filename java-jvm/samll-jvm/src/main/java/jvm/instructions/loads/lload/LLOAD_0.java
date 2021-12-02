package jvm.instructions.loads.lload;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

public class LLOAD_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        Long val = frame.localVars().getLong(0);
        frame.operandStack().pushLong(val);
    }

}
