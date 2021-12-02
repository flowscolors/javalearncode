package jvm.instructions.base;

import jvm.rtda.Frame;
import jvm.rtda.Slot;
import jvm.rtda.heap.methodarea.Method;
import jvm.rtda.Thread;

/**
 * @author flowscolors
 * @date 2021-11-12 15:03
 */
public class MethodInvokeLogic {
    public static void invokeMethod(Frame invokerFrame, Method method) {
        Thread thread = invokerFrame.thread();
        Frame newFrame = thread.newFrame(method);
        thread.pushFrame(newFrame);

        int argSlotCount = method.argSlotCount();
        if (argSlotCount > 0) {
            for (int i = argSlotCount - 1; i >= 0; i--) {
                Slot slot = invokerFrame.operandStack().popSlot();
                newFrame.localVars().setSlot(i, slot);
            }
        }

        //hack
        if (method.isNative()) {
            if ("registerNatives".equals(method.name())) {
                thread.popFrame();
            } else {
                throw new RuntimeException("native method " + method.name());
            }
        }
    }
}
