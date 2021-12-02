package jvm.instructions.stores.astore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class ASTORE_2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _astore(frame, 2);
    }

}

