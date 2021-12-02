package jvm.instructions.comparisons.ifcond;

import jvm.instructions.base.Instruction;
import jvm.instructions.base.InstructionBranch;
import jvm.rtda.Frame;

public class IFEQ extends InstructionBranch {

    @Override
    public void execute(Frame frame) {
        int val = frame.operandStack().popInt();
        if (0 == val) {
            Instruction.branch(frame, this.offset);
        }
    }
}
