package jvm.instructions.stack.pop;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.OperandStack;

// Pop the top one or two operand stack values
/*
bottom -> top
[...][c][b][a]
         |  |
         V  V
[...][c]
*/
public class POP2 extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        stack.popSlot();
        stack.popSlot();
    }

}
