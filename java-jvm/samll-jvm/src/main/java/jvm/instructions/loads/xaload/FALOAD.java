package jvm.instructions.loads.xaload;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.methodarea.Object;
/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/29
 */
public class FALOAD extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        OperandStack stack = frame.operandStack();
        int idx = stack.popInt();
        Object arrRef = stack.popRef();

        checkNotNull(arrRef);
        float[] floats = arrRef.floats();
        checkIndex(floats.length, idx);
        stack.pushFloat(floats[idx]);
    }

}