package jvm.instructions.loads.aload;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.heap.methodarea.Object;

public class ALOAD_0 extends InstructionNoOperands {
    
    @Override
    public void execute(Frame frame) {
        Object ref = frame.localVars().getRef(0);
        frame.operandStack().pushRef(ref);
    }

}
