package jvm.instructions.stores.xastore;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.methodarea.Object;
/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/29
 */
public class SASTORE extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int val = stack.popInt();
        int idx = stack.popInt();
        Object arrRef = stack.popRef();

        checkNotNull(arrRef);
        short[] shorts = arrRef.shorts();
        checkIndex(shorts.length, idx);
        shorts[idx] = (short) val;
    }

}
