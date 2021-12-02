package jvm.instructions.control;

import jvm.instructions.base.Instruction;
import jvm.instructions.base.InstructionBranch;
import jvm.rtda.Frame;

//branch always
public class GOTO extends InstructionBranch {

    @Override
    public void execute(Frame frame) {
        Instruction.branch(frame, this.offset);
    }
}
