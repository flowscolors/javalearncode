package Atomic;

/**
 * @author flowscolors
 * @date 2021-11-03 15:48
 */
public class CASDemo implements Runnable{
    private volatile int value;

    public synchronized int compareAndSwap(int expectedValue, int newValue) {

        int oldValue = value;

        if (oldValue == expectedValue) {

            value = newValue;

            System.out.println("线程"+Thread.currentThread().getName()+"执行成功");

        }

        return oldValue;

    }

    public static void main(String[] args) throws InterruptedException {

        CASDemo r = new CASDemo();

        r.value = 100;

        Thread t1 = new Thread(r,"Thread 1");

        Thread t2 = new Thread(r,"Thread 2");

        t1.start();

        t2.start();

        t1.join();

        t2.join();

        System.out.println(r.value);

    }

    @Override

    public void run() {

        compareAndSwap(100, 150);

    }
}
