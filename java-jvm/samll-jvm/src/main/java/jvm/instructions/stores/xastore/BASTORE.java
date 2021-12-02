package jvm.instructions.stores.xastore;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.methodarea.Object;
/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/29
 */
public class BASTORE extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int val = stack.popInt();
        int idx = stack.popInt();
        Object arrRef = stack.popRef();

        checkNotNull(arrRef);
        byte[] bytes = arrRef.bytes();
        checkIndex(bytes.length, idx);
        bytes[idx] = (byte) val;

    }

}
