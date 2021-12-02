package leetcode;

import com.sun.applet2.AppletParameters;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author flowscolors
 * @date 2021-10-27 10:20
 */
public class ProCon_BlockingQueue2 {

    static private ArrayBlockingQueue queue = new ArrayBlockingQueue<Integer>(10);

    public static void main(String[] args) {

        Thread producerThread = new Thread(() -> {
            try {
                while (true){
                    queue.put(1);
                    System.out.println("当前队列长度为 ： "+queue.size());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                while (true){
                    queue.take();
                    System.out.println("当前队列长度为 ： "+queue.size());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producerThread.start();
        consumerThread.start();

    }
}
