package Synchronized;

/**
 * @author flowscolors
 * @date 2021-10-24 9:46
 */
public class DeadLock_Synchronized {
    public static void main(String[] args) {
        Object object1 = new Object();
        Object object2 = new Object();

        Thread thread1 = new Thread(() -> {
            while (true){
                synchronized (object1){
                    try {
                        Thread.sleep(3000);
                        synchronized (object2){
                            System.out.println("Lock1 lock object2");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            while (true){
                synchronized (object2){
                    try {
                        Thread.sleep(3000);
                        synchronized (object1){
                            System.out.println("Lock2 lock object1");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread1.start();
        thread2.start();
    }
}
