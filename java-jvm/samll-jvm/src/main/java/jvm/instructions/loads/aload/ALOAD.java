package jvm.instructions.loads.aload;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;
import  jvm.rtda.heap.methodarea.Object;

//load reference from local variable
public class ALOAD extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        Object ref = frame.localVars().getRef(this.idx);
        frame.operandStack().pushRef(ref);
    }

}
