package jvm.instructions.stores.lstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class LSTORE_0 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _lstore(frame, 0);
    }

}
