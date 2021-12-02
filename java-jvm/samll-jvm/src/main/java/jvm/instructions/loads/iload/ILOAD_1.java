package jvm.instructions.loads.iload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class ILOAD_1 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        int val = frame.localVars().getInt(1);
        frame.operandStack().pushInt(val);
    }
}

