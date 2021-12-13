import jdk.nashorn.internal.ir.IdentNode;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2021-12-10 13:42
 */
public class MyArrayListDemo2 {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> arrayList = new MyArrayList<>();
        new Thread(() -> ((MyArrayList<Integer>) arrayList).add2(9), "My1").start();
        new Thread(() -> ((MyArrayList<Integer>) arrayList).add2(10), "My2").start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("arrayList = " + arrayList + ",size=" + arrayList.size());
    }
}
