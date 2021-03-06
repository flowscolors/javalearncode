package jvm.instructions.math.sh;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.OperandStack;

//arithmetic shift right int
public class IUSHR extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int v2 = stack.popInt();
        int v1 = stack.popInt();
        int s = v2 & 0x1f;
        int res = v1 >> s;
        stack.pushInt(res);
    }

}

