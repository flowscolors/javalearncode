package jvm.instructions.loads.lload;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class LLOAD extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        Long val = frame.localVars().getLong(this.idx);
        frame.operandStack().pushLong(val);
    }

}
