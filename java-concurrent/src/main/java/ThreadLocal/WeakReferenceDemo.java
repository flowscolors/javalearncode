package ThreadLocal;

import java.lang.ref.WeakReference;

/**
 * @author flowscolors
 * @date 2021-12-10 1:43
 */
public class WeakReferenceDemo {
    public static void main(String[] args) {
        WeakReference<Fruit> fruitWeakReference = new WeakReference<>(new Fruit());
        //通过fruitWeakReference.get()，可以得到弱引用指向的对象，当执行System.gc()后，该对象被回收。对于弱引用，我们想知道它的状态，却不能通过普通的Java代码调用出它本身来观测它。
        //如果像下面用一个变量f指向fruitWeakReference.get()，不过就是将一个强引用指向了原本由弱引用指向的对象而已，此时对象被强引用 不会垃圾回收
        // Fruit f = fruitWeakReference.get();
        if (fruitWeakReference.get() != null) {
            System.out.println("Before GC, this is the result");
        }
        System.gc();
        if (fruitWeakReference.get() != null) {
            System.out.println("After GC, fruitWeakReference.get() is not null");
        } else {
            System.out.println("After GC, fruitWeakReference.get() is null");
        }
    }
}

class Fruit {
}