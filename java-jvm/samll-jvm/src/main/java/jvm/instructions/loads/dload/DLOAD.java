package jvm.instructions.loads.dload;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class DLOAD extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        double val = frame.localVars().getDouble(this.idx);
        frame.operandStack().pushDouble(val);
    }

}