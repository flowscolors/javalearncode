package jvm.rtda;

/**
 * @author flowscolors
 * @date 2021-11-11 16:42
 */
//Java虚拟机栈 实际链表并不需要知道整个 只要知道头节点即可，这里就是这样 提供对Java虚拟机栈的 出栈、入栈、获得top方法
public class JvmStack {

    private int maxSize;

    private int size;

    private Frame _top;

    public JvmStack(int maxSize){
        this.maxSize = maxSize;
    }

    public void push(Frame frame){
        if(this.size > this.maxSize){
            throw new StackOverflowError();
        }
        if(this._top!=null){
            frame = this._top;
        }
        this._top = frame;
        size++;
    }

    public Frame pop(){
        if(this._top==null){
            throw new RuntimeException("jvm stack is empty!");
        }
        Frame top = this._top;
        this._top = top.lower;
        top.lower = null;
        this.size--;
        return top;
    }

    public Frame top(){
        if(this._top==null){
            throw new RuntimeException("jvm stack is empty!");
        }
        return this._top;
    }

    public boolean isEmpty(){
        return this._top == null;
    }
}
