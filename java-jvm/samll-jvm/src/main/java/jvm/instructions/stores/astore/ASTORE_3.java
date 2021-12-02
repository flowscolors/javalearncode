package jvm.instructions.stores.astore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class ASTORE_3 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _astore(frame, 3);
    }

}
