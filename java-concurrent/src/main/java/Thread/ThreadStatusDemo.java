package Thread;

import org.openjdk.jol.info.ClassLayout;

import static java.lang.Thread.sleep;

/**
 * @author flowscolors
 * @date 2021-10-26 14:42
 */
public class ThreadStatusDemo {
    public static void main(String[] args) throws InterruptedException {
        Object object = new Object();
        Thread thread = new Thread(() -> {
            System.out.println("运行中");
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        System.out.println(thread.getName()+" 状态 "+ thread.getState()+"  已创建线程但是尚未执行任务");

/*        new Thread(() -> {
            synchronized (object){
                System.out.println("  ** other线程获取到object锁 ** "+Thread.currentThread().getName());
                try {
                    sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/

        //Java 中的 Runable 状态对应操作系统线程状态中的两种状态，分别是 Running 和 Ready，
        // 也就是说，Java 中处于 Runnable 状态的线程有可能正在执行，也有可能没有正在执行，正在等待被分配 CPU 资源。
        thread.start();
        System.out.println(thread.getName()+" 状态 "+ thread.getState()+"  执行start方法,进入可运行状态");

        //Java中阻塞状态包括三种状态，分别是 Blocked(被阻塞）、Waiting(等待）、Timed Waiting(计时等待）。

        synchronized (thread) {
            thread.wait();
        }
        System.out.println(thread.getName()+" 状态 "+ thread.getState()+"  ");

    }
}
