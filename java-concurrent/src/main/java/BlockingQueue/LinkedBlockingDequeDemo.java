package BlockingQueue;

import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author flowscolors
 * @date 2021-10-23 10:25
 */
public class LinkedBlockingDequeDemo {

    //private static Queue<String> queue = new LinkedList<String>();
    //private static Queue<String> queue = new LinkedBlockingDeque<String>();
    private static LinkedBlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    //private static Queue<String> queue = new LinkedBlockingQueue<String>();
    //private static Queue<String> queue = new ArrayBlockingQueue<String>(20);


    public static void main(String[] args) {
        new MyThread("A").start();
        new MyThread("B").start();
    }

    private static void printAll() {
        String value;
        Iterator iter = queue.iterator();
        while (iter.hasNext()) {
            value = (String) iter.next();
            System.out.print(value + ", ");
        }
        System.out.println();
    }

    private static class MyThread extends Thread{
        MyThread(String name){
            super(name);
        }

        //每次插入一个值 并打印当前队列中所有值  每次插入时是拿锁 插入完释放锁 别的线程可以插入
        @SneakyThrows
        @Override
        public void run() {
            for(int i = 0;i<6;i++){
                queue.put(Thread.currentThread().getName()+i);
                printAll();
            }
        }
    }
}


