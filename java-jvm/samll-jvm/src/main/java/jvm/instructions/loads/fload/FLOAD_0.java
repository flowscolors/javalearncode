package jvm.instructions.loads.fload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class FLOAD_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        Float val = frame.localVars().getFloat(0);
        frame.operandStack().pushFloat(val);
    }

}
