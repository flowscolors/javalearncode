package jvm.rtda;

import jvm.rtda.heap.methodarea.Method;

/**
 * @author flowscolors
 * @date 2021-11-11 16:28
 */
//栈帧 作为Java虚拟机栈的一个节点，每个栈帧中都有局部变量表和操作数栈，用来对方法进行计算
public class Frame {

    //栈做为一个链表，每个节点需要存下一个节点信息
    Frame lower;

    private LocalVars localVars;

    private OperandStack operandStack;

    //要存栈帧对应的当前线程 和下一条栈帧对应的程序计数器位置 每次获取nextPC去执行对应指令 如果算错了 影响巨大
    private Thread thread;

    private int nextPC;

    private Method method;

/*  //for old
    public Frame(Thread thread, int maxLocals, int maxStack) {
        this.thread = thread;
        this.localVars = new LocalVars(maxLocals);
        this.operandStack = new OperandStack(maxStack);
    }*/

    public Frame(Thread thread, Method method) {
        this.thread = thread;
        this.method = method;
        this.localVars = new LocalVars(method.maxLocals);
        this.operandStack = new OperandStack(method.maxStack);
    }

    public LocalVars localVars() {
        return localVars;
    }

    public OperandStack operandStack() {
        return operandStack;
    }

    public Thread thread() {
        return this.thread;
    }

    public Method method(){
        return this.method;
    }

    public int nextPC() {
        return this.nextPC;
    }

    public void setNextPC(int nextPC) {
        this.nextPC = nextPC;
    }

    public void revertNextPC(){
        this.nextPC = this.thread.pc();
    }

}
