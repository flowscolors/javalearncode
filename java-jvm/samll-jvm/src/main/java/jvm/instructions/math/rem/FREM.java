package jvm.instructions.math.rem;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.OperandStack;

//remainder float
public class FREM extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        float v2 = stack.popFloat();
        float v1 = stack.popFloat();
        float res = v1 % v2;
        stack.pushFloat(res);
    }

}