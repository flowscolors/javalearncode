package jvm.instructions.stores.istore;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class ISTORE extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        _istore(frame, this.idx);
    }

}