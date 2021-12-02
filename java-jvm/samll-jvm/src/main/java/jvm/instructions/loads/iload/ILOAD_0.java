package jvm.instructions.loads.iload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class ILOAD_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        int val = frame.localVars().getInt(0);
        frame.operandStack().pushInt(val);
    }
}
