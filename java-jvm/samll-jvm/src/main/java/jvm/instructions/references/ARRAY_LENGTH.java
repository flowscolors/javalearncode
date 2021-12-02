package jvm.instructions.references;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Object;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/29
 * get length of array
 */
public class ARRAY_LENGTH extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {

        OperandStack stack = frame.operandStack();
        Object arrRef = stack.popRef();
        if (null == arrRef){
            throw new NullPointerException();
        }

        int arrLen = arrRef.arrayLength();
        stack.pushInt(arrLen);
    }

}
