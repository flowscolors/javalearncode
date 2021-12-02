package jvm.instructions.stores.fstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class FSTORE_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _fstore(frame, 0);
    }

}