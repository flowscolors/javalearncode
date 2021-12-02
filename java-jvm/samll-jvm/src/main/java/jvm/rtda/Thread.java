package jvm.rtda;

import jvm.rtda.heap.methodarea.Method;

/**
 * @author flowscolors
 * @date 2021-11-11 16:50
 */
//线程 线程独占区域有程序计数器、Java虚拟机栈
public class Thread {

    //Program Counter 寄存器
    private int pc;

    private JvmStack stack;

    public Thread() {
        this.stack = new JvmStack(1024);
    }

    public int pc(){
        return this.pc;
    }

    public void setPC(int pc){
        this.pc = pc;
    }

    //线程封装了对Java虚拟机栈的操作 直接给线程传栈帧即可
    public void pushFrame(Frame frame){
        this.stack.push(frame);
    }

    public Frame popFrame(){
        return this.stack.pop();
    }

    public Frame currentFrame(){
        return this.stack.top();
    }

    public Frame topFrame(){
        return this.stack.top();
    }

    public boolean isStackEmpty(){
        return this.stack.isEmpty();
    }

    public Frame newFrame(Method method) {
        return new Frame(this, method);
    }

}
