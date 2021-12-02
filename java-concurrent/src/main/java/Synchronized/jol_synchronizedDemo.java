package Synchronized;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author flowscolors
 * @date 2021-10-23 18:17
 */
public class jol_synchronizedDemo {
    public static void main(String[] args) throws InterruptedException {
        Object object = new Object();
        System.out.println("1.无锁状态 001");
        System.out.println(ClassLayout.parseInstance(object).toPrintable());

        //jvm开启4s后会对每个新建的对象加默认的偏向锁，之前创建的不受影响。
        Thread.sleep(5000);
        System.out.println("2.偏向锁 101  main线程第一次获取锁 升级为偏向锁");
        //分别代表默认的创建的匿名的偏向锁 以及一个线程去获取锁对象的场景，后者会把锁的线程ID放到对象的Markword中
        Object object2 = new Object();
        //System.out.println(ClassLayout.parseInstance(object2).toPrintable());
        synchronized (object2){
            //当前锁对象第一次被线程获取
        }
        System.out.println(ClassLayout.parseInstance(object2).toPrintable());


        System.out.println("3.轻量级锁 00 object2第一次被其他线程获取");
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (object2){
                    //当前锁对象第一次被其他线程获取 因为jol的并发关系 此时其他线程看到的是轻量级锁 main线程看到的依旧是偏向锁
                    System.out.println(ClassLayout.parseInstance(object2).toPrintable());

                }
            }
        }).start();
        //System.out.println(ClassLayout.parseInstance(object2).toPrintable());



        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (object2){
                        System.out.println("4 重量级锁加锁中 10 "+Thread.currentThread().getName());
                        System.out.println(ClassLayout.parseInstance(object2).toPrintable());
                    }
                }
            }).start();
        }

        Thread.sleep(2000);
        System.out.println("5. 无锁 001 因为最后所有线程都释放了锁，于是变为无锁状态");
        System.out.println(ClassLayout.parseInstance(object2).toPrintable());


    }
}
