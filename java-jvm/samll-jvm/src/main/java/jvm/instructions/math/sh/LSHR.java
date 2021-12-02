package jvm.instructions.math.sh;

import  jvm.instructions.base.InstructionNoOperands;
import  jvm.rtda.Frame;
import  jvm.rtda.OperandStack;

// Arithmetic shift right long
public class LSHR  extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int v2 = stack.popInt();
        long v1 = stack.popLong();
        long s = v2 & 0x3f;
        long res = v1 >> s;
        stack.pushLong(res);
    }

}

