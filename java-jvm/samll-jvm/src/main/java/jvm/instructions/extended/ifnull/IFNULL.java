package jvm.instructions.extended.ifnull;

import jvm.instructions.base.Instruction;
import jvm.instructions.base.InstructionBranch;
import jvm.rtda.Frame;

//branch if reference is null
public class IFNULL extends InstructionBranch {

    @Override
    public void execute(Frame frame) {
        Object ref = frame.operandStack().popRef();
        if (null == ref) {
            Instruction.branch(frame, this.offset);
        }
    }
}
