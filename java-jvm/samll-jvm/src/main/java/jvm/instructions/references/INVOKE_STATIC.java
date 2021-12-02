package jvm.instructions.references;

import jvm.instructions.base.ClassInitLogic;
import jvm.rtda.Frame;
import jvm.instructions.base.InstructionIndex16;
import jvm.instructions.base.MethodInvokeLogic;
import jvm.rtda.heap.constantpool.MethodRef;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Method;

/**
 * @author flowscolors
 * @date 2021-11-12 15:11
 */
public class INVOKE_STATIC extends InstructionIndex16 {
    @Override
    public void execute(Frame frame) {
        RunTimeConstantPool runTimeConstantPool = frame.method().clazz().constantPool();
        MethodRef methodRef = (MethodRef) runTimeConstantPool.getConstants(this.idx);
        Method resolvedMethod = methodRef.ResolvedMethod();

        if (!resolvedMethod.isStatic()) {
            throw new IncompatibleClassChangeError();
        }

        Class clazz = resolvedMethod.clazz();
        if (!clazz.initStarted()) {
            frame.revertNextPC();
            ClassInitLogic.initClass(frame.thread(), clazz);
            return;
        }

        MethodInvokeLogic.invokeMethod(frame, resolvedMethod);
    }
}
