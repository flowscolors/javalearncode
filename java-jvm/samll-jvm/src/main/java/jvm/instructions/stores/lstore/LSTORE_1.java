package jvm.instructions.stores.lstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class LSTORE_1 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _lstore(frame, 1);
    }

}
