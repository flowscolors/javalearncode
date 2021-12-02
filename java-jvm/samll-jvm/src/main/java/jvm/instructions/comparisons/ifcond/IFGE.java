package jvm.instructions.comparisons.ifcond;

import jvm.instructions.base.Instruction;
import jvm.instructions.base.InstructionBranch;
import jvm.rtda.Frame;

public class IFGE extends InstructionBranch {

    @Override
    public void execute(Frame frame) {
        int val = frame.operandStack().popInt();
        if (val >= 0) {
            Instruction.branch(frame, this.offset);
        }
    }
}