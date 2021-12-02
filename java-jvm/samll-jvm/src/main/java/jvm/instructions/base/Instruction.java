package jvm.instructions.base;

import jvm.rtda.Frame;

/**
 * @author flowscolors
 * @date 2021-11-11 21:52
 */
public interface Instruction {
    void fetchOperands(BytecodeReader reader);

    void execute(Frame frame);

    static void branch(Frame frame, int offset) {
        int pc = frame.thread().pc();
        int nextPC = pc + offset;
        frame.setNextPC(nextPC);
    }
}
