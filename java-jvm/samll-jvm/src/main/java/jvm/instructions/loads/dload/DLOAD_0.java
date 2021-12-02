package jvm.instructions.loads.dload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class DLOAD_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        double val = frame.localVars().getDouble(0);
        frame.operandStack().pushDouble(val);
    }

}