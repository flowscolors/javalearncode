package jvm.rtda;

import jvm.rtda.heap.methodarea.Object;
/**
 * @author flowscolors
 * @date 2021-11-11 16:15
 */
// 数据槽 局部变量表、操作数栈中最基本的数据结构，本质是一个int值和一个对象的引用
// 注意需要查看时，属性值改为public，否则默认是protected，外界无法访问
public class Slot {
    public int num;
    public Object ref;
}
