package jvm.instructions.stores.fstore;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class FSTORE extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        _fstore(frame, this.idx);
    }

}
