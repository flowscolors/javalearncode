package jvm;

import com.alibaba.fastjson.JSON;
import jvm.instructions.Factory;
import jvm.instructions.base.BytecodeReader;
import jvm.instructions.base.Instruction;
import jvm.rtda.Frame;
import jvm.rtda.Thread;
import jvm.rtda.heap.methodarea.Method;

/**
 * @author flowscolors
 * @date 2021-11-11 22:31
 */
//指令集解释器
public class Interpret {

/*    //for old
    Interpret(MemberInfo m) {
        CodeAttribute codeAttr = m.codeAttribute();
        int maxLocals = codeAttr.maxLocals();
        int maxStack = codeAttr.maxStack();
        byte[] byteCode = codeAttr.data();
        Thread thread = new Thread();
        Frame frame = thread.newFrame(maxLocals, maxStack);
        thread.pushFrame(frame);
        loop(thread, byteCode);
    }*/

/*    Interpret(Method method) {
        Thread thread = new Thread();
        Frame frame = thread.newFrame(method);
        thread.pushFrame(frame);
        loop(thread, method.code());
    }*/

    Interpret(Method method, boolean logInst) {
        Thread thread = new Thread();
        Frame frame = thread.newFrame(method);
        thread.pushFrame(frame);

        loop(thread, logInst);
    }

    //核心依旧是读取指令码，生成对应指令，对操作数表和程序计数器进行计算。
    private void loop(Thread thread, boolean logInst) {
        BytecodeReader reader = new BytecodeReader();
        while (true) {
            //获取当前方法帧，获取下一步执行的程序计数器值，并跳转到对应位置
            Frame frame = thread.currentFrame();
            int pc = frame.nextPC();
            thread.setPC(pc);
            reader.reset(frame.method().code, pc);
            //获取操作码 生成操作指令
            byte opcode = reader.readByte();
            Instruction inst = Factory.newInstruction(opcode);
            if (null == inst) {
                System.out.println("Unsupported opcode " + byteToHexString(new byte[]{opcode}));
                break;
            }
            //获取操作数表 执行指令 获得下一位置pc 存到栈帧的nextPC中供下次循环使用
            inst.fetchOperands(reader);
            frame.setNextPC(reader.pc());
            logInstruction(frame, inst, opcode);
            inst.execute(frame);
            if (thread.isStackEmpty()) {
                break;
            }
        }
    }

    private static void logInstruction(Frame frame, Instruction inst, byte opcode) {
        Method method = frame.method();
        String className = method.clazz().name();
        String methodName = method.name();
        String outStr = (className + "." + methodName + "() \t") +
                "寄存器(指令)：" + byteToHexString(new byte[]{opcode}) + " -> " + inst.getClass().getSimpleName() + " => 局部变量表：" + JSON.toJSONString(frame.localVars().getSlots()) + " 操作数栈：" + JSON.toJSONString(frame.operandStack().getSlots());
        System.out.println(outStr);
    }

    private static String byteToHexString(byte[] codes) {
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : codes) {
            int value = b & 0xFF;
            String strHex = Integer.toHexString(value);
            if (strHex.length() < 2) {
                strHex = "0" + strHex;
            }
            sb.append(strHex);
        }
        return sb.toString();
    }
}
