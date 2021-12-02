package leetcode;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author flowscolors
 * @date 2021-10-27 10:37
 */
public class MyBlockingQueue_Condition {

    //自己基于Condition实现一个阻塞队列
    //定义了一个队列变量 queue 并设置最大容量为 16
    private Queue queue;

    private int max = 16;

    //定义了一个 ReentrantLock 类型的 Lock 锁，并在 Lock 锁的基础上创建两个 Condition，
    // 一个是 notEmpty，另一个是 notFull，分别代表队列没有空和没有满的条件

    private ReentrantLock lock = new ReentrantLock();

    private Condition notEmpty = lock.newCondition();

    private Condition notFull = lock.newCondition();

    public MyBlockingQueue_Condition(int size) {

        this.max = size;

        queue = new LinkedList();

    }

    //先锁lock，finally解锁，while循环判是否满，已满则调用notFull的await()阻塞生产者线程并释放Lock。
    //如果没满则把数据放入，并通知所有正在等待的消费者并唤醒他们。注意这里用while而非if，在多线程存入消费的时候多进行以此判断
    public void put(Object o) throws InterruptedException {

        lock.lock();

        try {

            while (queue.size() == max) {

                notFull.await();

            }

            queue.add(o);

            notEmpty.signalAll();

        } finally {

            lock.unlock();

        }

    }

    public Object take() throws InterruptedException {

        lock.lock();

        try {

            while (queue.size() == 0) {

                notEmpty.await();

            }

            Object item = queue.remove();

            notFull.signalAll();

            return item;

        } finally {

            lock.unlock();

        }

    }

}
