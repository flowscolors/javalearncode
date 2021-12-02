package jvm.instructions.stores.fstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class FSTORE_2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _fstore(frame, 2);
    }

}
