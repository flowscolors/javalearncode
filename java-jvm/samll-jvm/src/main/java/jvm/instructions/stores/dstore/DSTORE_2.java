package jvm.instructions.stores.dstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class DSTORE_2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _dstore(frame, 2);
    }

}

