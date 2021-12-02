package jvm.instructions.loads.fload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class FLOAD_1 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        Float val = frame.localVars().getFloat(1);
        frame.operandStack().pushFloat(val);
    }

}
