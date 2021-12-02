package jvm.instructions.comparisons.if_icmp;

import jvm.instructions.base.Instruction;
import jvm.instructions.base.InstructionBranch;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

public class IF_ICMPGE extends InstructionBranch {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int val2 = stack.popInt();
        int val1 = stack.popInt();
        if (val1 >= val2) {
            Instruction.branch(frame, this.offset);
        }
    }

}
