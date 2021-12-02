package jvm.instructions.stores.istore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class ISTORE_3 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _istore(frame, 3);
    }

}

