package jvm.instructions.stores.lstore;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;

public class LSTORE_2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        _lstore(frame, 2);
    }

}
