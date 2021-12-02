package jvm.instructions.control.rtn;

import jvm.instructions.base.InstructionNoOperands;
import jvm.rtda.Frame;
import jvm.rtda.Thread;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/27
 */
public class DRETURN extends InstructionNoOperands {

    @Override
    public void execute(Frame frame) {
        Thread thread = frame.thread();
        Frame currentFrame = thread.popFrame();
        Frame invokerFrame = thread.topFrame();
        double val = currentFrame.operandStack().popDouble();
        invokerFrame.operandStack().pushDouble(val);
    }

}

