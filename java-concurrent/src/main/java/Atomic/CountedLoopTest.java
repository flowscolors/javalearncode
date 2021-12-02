package Atomic;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author flowscolors
 * @date 2021-10-05 14:50
 */
public class CountedLoopTest {
    //public static AtomicInteger num = new AtomicInteger(0);
    public volatile  static int num = 0;

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            for (int i = 0; i < 1000000000 ; i++) {
                //num.getAndAdd(1);
                num++;
            }
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();

        System.out.println("before sleep");
        Thread.sleep(1000);
        System.out.println("after sleep");

        System.out.println(num);
    }
}
