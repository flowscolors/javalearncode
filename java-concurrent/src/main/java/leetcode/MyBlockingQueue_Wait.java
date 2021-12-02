package leetcode;

import java.util.LinkedList;

/**
 * @author flowscolors
 * @date 2021-10-27 11:11
 */
public class MyBlockingQueue_Wait {

    //基于wait/notify实现的阻塞队列
    //最主要的部分仍是被 synchronized 保护的 take 与 put 方法，while检查队列是否满，不满则放入数据并notifyAll()，
    private int maxSize;

    private LinkedList<Object> storage;

    public MyBlockingQueue_Wait(int size) {

        this.maxSize = size;

        storage = new LinkedList<>();

    }

    public synchronized void put() throws InterruptedException {

        while (storage.size() == maxSize) {

            wait();

        }

        storage.add(new Object());

        notifyAll();

    }

    public synchronized void take() throws InterruptedException {

        while (storage.size() == 0) {

            wait();

        }

        System.out.println(storage.remove());

        notifyAll();

    }

}
