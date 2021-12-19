package Synchronized;

/**
 * @author flowscolors
 * @date 2021-12-19 15:29
 */
public class UnFairSynchronizedDemo {
    public static void main(String[] args) {

        UnFairSynchronizedDemo syncDemo1 = new UnFairSynchronizedDemo();
        syncDemo1.startThreadA();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        syncDemo1.startThreadB();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        syncDemo1.startThreadC();


    }

    final Object lock = new Object();


    public void startThreadA() {
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("A get lock");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("A release lock");
            }
        }, "thread-A").start();
    }

    public void startThreadB() {
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("B get lock");
            }
        }, "thread-B").start();
    }

    public void startThreadC() {
        new Thread(() -> {
            synchronized (lock) {

                System.out.println("C get lock");
            }
        }, "thread-C").start();
    }
}
