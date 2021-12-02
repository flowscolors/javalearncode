package jvm.instructions.math.div;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.OperandStack;

//divide int
public class IDIV extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int v2 = stack.popInt();
        int v1 = stack.popInt();
        if (v2 == 0){
            throw new ArithmeticException("/ by zero");
        }
        int res = v1 / v2;
        stack.pushInt(res);
    }

}
