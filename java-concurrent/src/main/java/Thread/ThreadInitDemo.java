package Thread;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @author flowscolors
 * @date 2021-10-26 10:09
 */
public class ThreadInitDemo {

    static class MyThread1 extends Thread {     //成员内部类
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+
                    " 1.方法一 继承Thread类，并重写run方法。创建子类对象，并执行start方法");
        }
    }

    static class MyThread2 implements Runnable {     //成员内部类
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+
                    " 2.方法二 实现Runable接口，并重写run方法。创建对象，放入线程或线程池，并执行start方法");
        }
    }

    static class MyThread3 implements Callable {     //成员内部类
        @Override
        public Object call() throws Exception {
            System.out.println(Thread.currentThread().getName()+
                    " 3.方法3 实现Callable接口，并重写call方法。创建对象，放入FutureTask,将task线程或线程池，并执行start方法");
            return new Random().nextInt(100);
        }
    }

    static class MyThread4 implements Runnable {     //成员内部类
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+
                    " 4.方法四 实现Runable接口或Callable接口，并重写run、call方法。创建对象，放入线程池，线程池执行execute。当然也有lambda表达式的形式");
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        MyThread1 myThread1 = new MyThread1();
        myThread1.start();

        Thread thread2 = new Thread(new MyThread2());
        thread2.start();

        FutureTask<Integer> task = new FutureTask(new MyThread3());
        Thread thread3 = new Thread(task);
        thread3.start();
        System.out.println(Thread.currentThread().getName()+"   "+task.get()
                +"   最后可以通过FutureTask的get()方法获取线程执行结果");

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for(int i = 0;i<5;i++){
            executorService.execute(new MyThread4());
        }
        executorService.shutdown();

        //如果想要调试创建thread和实现runnable接口的不同 断点打Thread.java的744行 。
        // new Thread直接调子类run，实现runnable接口由thread去调实现类run方法。
    }
}

