package AQS;

import java.util.concurrent.Semaphore;

/**
 * @author flowscolors
 * @date 2021-10-05 14:53
 */
//Semaphore实现生产者消费者
public class SemaphoreProAndCom {
    private static Integer length = 100;
    private static Integer index = 0;
    private static Semaphore notFull = new Semaphore(length);
    private static Semaphore notEmpty = new Semaphore(0);
    private static Semaphore mutex = new Semaphore(1);

    public void produce()  {
        try {
            notFull.acquire();
            mutex.acquire();
            index++;
            System.out.println("生成者生产,当前队列长度"+index);
            mutex.release();
            notEmpty.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void comsume()  {

        try {
            notEmpty.acquire();
            mutex.acquire();
            index--;
            System.out.println("消费者消费，当前队列长度"+index);
            mutex.release();
            notFull.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SemaphoreProAndCom proceduceAndComsume = new SemaphoreProAndCom();
        new Thread(()-> proceduceAndComsume.produce()).start();
        new Thread(()-> proceduceAndComsume.produce()).start();
        new Thread(()-> proceduceAndComsume.produce()).start();
        new Thread(()-> proceduceAndComsume.comsume()).start();
        new Thread(()-> proceduceAndComsume.comsume()).start();
    }
}
