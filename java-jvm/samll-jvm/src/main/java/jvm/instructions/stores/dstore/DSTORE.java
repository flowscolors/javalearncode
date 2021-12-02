package jvm.instructions.stores.dstore;

import  jvm.instructions.base.InstructionIndex8;
import  jvm.rtda.Frame;

public class DSTORE extends InstructionIndex8 {

    @Override
    public void execute(Frame frame) {
        _astore(frame, this.idx);
    }

}
