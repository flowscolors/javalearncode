package jvm.instructions.loads.dload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class DLOAD_2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        double val = frame.localVars().getDouble(2);
        frame.operandStack().pushDouble(val);
    }


}

