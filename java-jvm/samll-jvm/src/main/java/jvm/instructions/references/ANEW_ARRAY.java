package jvm.instructions.references;

import jvm.instructions.base.InstructionIndex16;
import jvm.rtda.Frame;
import jvm.rtda.OperandStack;
import jvm.rtda.heap.constantpool.ClassRef;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Object;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/29
 * create new array of reference
 */
public class ANEW_ARRAY extends InstructionIndex16 {

    @Override
    public void execute(Frame frame) {
        
        RunTimeConstantPool runTimeConstantPool = frame.method().clazz().constantPool();
        ClassRef classRef = (ClassRef) runTimeConstantPool.getConstants(this.idx);
        Class componentClass = classRef.resolvedClass();

        OperandStack stack = frame.operandStack();
        int count = stack.popInt();
        if (count < 0) {
            throw new NegativeArraySizeException();
        }

        Class arrClass = componentClass.arrayClass();
        Object arr = arrClass.newArray(count);
        stack.pushRef(arr);

    }

}
