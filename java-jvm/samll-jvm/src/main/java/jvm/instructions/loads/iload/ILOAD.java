package jvm.instructions.loads.iload;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

//load int from local variable
public class ILOAD extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        int val = frame.localVars().getInt(this.idx);
        frame.operandStack().pushInt(val);
    }
}

