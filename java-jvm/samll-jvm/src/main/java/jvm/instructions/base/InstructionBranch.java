package jvm.instructions.base;

import jvm.rtda.Frame;
import jvm.rtda.OperandStack;

/**
 * @author flowscolors
 * @date 2021-11-11 21:54
 */
//_acmp在指令集中代表比较器
public class InstructionBranch implements Instruction {

    protected int offset;

    @Override
    public void fetchOperands(BytecodeReader reader) {
        this.offset = reader.readShort();
    }

    @Override
    public void execute(Frame frame) {

    }

    protected boolean _acmp(Frame frame){
        OperandStack stack = frame.operandStack();
        Object ref2 = stack.popRef();
        Object ref1 = stack.popRef();
        return ref1.equals(ref2);
    }

}
