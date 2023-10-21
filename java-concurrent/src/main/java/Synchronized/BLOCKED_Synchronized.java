package Synchronized;

import java.util.Scanner;

/**
 * @author flowscolors
 * @date 2021-12-10 1:12
 */
public class BLOCKED_Synchronized {
    public static void main(String[] args) {
        byte[] lock = new byte[0];
        Runnable task = () -> {
            {
                synchronized (lock) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("waiting enter sth: ");
                    String s = scanner.next();
                    System.out.println(Thread.currentThread().getName() + ": " + s);
                }
            }
        };
        Thread t1 = new Thread(task, "t1");
        Thread t2 = new Thread(task, "t2");
        t1.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread1 getstate() =  "+t1.getState());
        t2.start();
        System.out.println("Thread2 getstate() =  "+t2.getState());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Thread2 getstate() =  "+t2.getState());
    }
}
