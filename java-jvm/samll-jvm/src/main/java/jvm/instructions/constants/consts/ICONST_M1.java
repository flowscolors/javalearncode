package jvm.instructions.constants.consts;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;

//push int constant
public class ICONST_M1 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        frame.operandStack().pushInt(-1);
    }
    
}
