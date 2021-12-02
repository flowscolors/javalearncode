package jvm.instructions.stores.lstore;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class LSTORE extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        _lstore(frame, this.idx);
    }

}
