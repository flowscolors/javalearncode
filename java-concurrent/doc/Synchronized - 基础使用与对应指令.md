
Synchronized是Java中解决并发问题的一种最常用的方法，也是最简单的一种方法。Synchronized的作用主要有三个：

原子性：确保线程互斥的访问同步代码；
可见性：保证共享变量的修改能够及时可见，其实是通过Java内存模型中的 “对一个变量unlock操作之前，必须要同步到主内存中；如果对一个变量进行lock操作，则将会清空工作内存中此变量的值，在执行引擎使用此变量前，需要重新从主内存中load操作或assign操作初始化变量值” 来保证的；
有序性：有效解决重排序问题，即 “一个unlock操作先行发生(happen-before)于后面对同一个锁的lock操作”；

从语法上讲，Synchronized可以把任何一个非null对象作为"锁"，在HotSpot JVM实现中，锁有个专门的名字：对象监视器（Object Monitor）。

## 1.synchronized 使用的三种方法
利用 synchronized 关键字来修饰代码块或者修饰一个方法，那么这部分被保护的代码，在同一时刻就最多只有一个线程可以运行

Synchronized总共有三种用法：

当synchronized作用在实例方法时，监视器锁（monitor）便是对象实例（this），锁代码段和锁实例方法一样。；
当synchronized作用在静态方法时，监视器锁（monitor）便是对象的Class实例，因为Class数据存在于永久代，因此静态方法锁相当于该类的一个全局锁；
当synchronized作用在某一个对象实例时，监视器锁（monitor）便是括号括起来的对象实例；但是锁对象不能为空，否则会抛出NPE(NullPointerException)。
当synchronized作用在某一个类时，该类的所有对象公用这把锁。

以上使用方式，其实本质还是作用于对象上。因为Java中的锁是在对象上，作用的区域不同，只是什么时候进入临界区去抢锁的区别。也即monitorenter、monitorexit包裹代码块的区别。
而synchronized修饰方法时，字节码里并没有monitorenter、monitorexit，而是修饰方法的时候在 flag 上标记 ACC_SYNCHRONIZED，在运行时常量池中通过 ACC_SYNCHRONIZED 标志来区分，这样 JVM 就知道这个方法是被 synchronized 标记的，于是在进入方法的时候就会进行执行争锁的操作，一样只有拿到锁才能继续执行。
对于flag 访问标志:0x0021 ，是 ACC_PUBLIC 和 ACC_SYNCHRONIZED

waiting code

* 当前线程想调用对象A的同步方法时，发现对象A的锁被别的线程占有，此时当前线程进入对象锁的同步队列。简言之，同步队列里面放的都是想争夺对象锁的线程。
* 当一个线程1被另外一个线程2唤醒时，1线程进入同步队列，去争夺对象锁。
* 同步队列是在同步的环境下才有的概念，一个对象对应一个同步队列。
* 线程等待时间到了或被notify/notifyAll唤醒后，会进入同步队列竞争锁，如果获得锁，进入RUNNABLE状态，否则进入BLOCKED状态等待获取锁。


## 2.synchronized 对应的锁

每个 Java 对象都可以用作一个实现同步的锁，这个锁也被称为内置锁或 monitor 锁，获得 monitor 锁的唯一途径就是进入由这个锁保护的同步代码块或同步方法，线程在进入被 synchronized 保护的代码块之前，会自动获取锁，并且无论是正常路径退出，还是通过抛出异常退出，在退出的时候都会自动释放锁。
一个线程获取一把锁，没有得到锁的线程只能进行等待排队【也有队列，这个队列就是C++中定义的队列了】。synchronized 是可重入锁，避免很多情况下的死锁发生。synchronized 方法若发生异常，则JVM会自动释放锁。

IDEA安装jclasslib进行代码字节码查看。  

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/synchronized-monenter.png)

synchronized 代码块实际上多了 monitorenter 和 monitorexit 指令，标红的第54、56、63行指令分别对应的是 monitorenter 和 monitorexit。这里有一个 monitorenter，却有两个 monitorexit 指令的原因是，JVM 要保证每个 monitorenter 必须有与之对应的 monitorexit，monitorenter 指令被插入到同步代码块的开始位置，而 monitorexit 需要插入到方法正常结束处和异常处两个地方，这样就可以保证抛异常的情况下也能释放锁

* monitorenter
执行 monitorenter 的线程尝试获得 monitor 的所有权，会发生以下这三种情况之一：

a. 如果该 monitor 的计数为 0，则线程获得该 monitor 并将其计数设置为 1。然后，该线程就是这个 monitor 的所有者。

b. 如果线程已经拥有了这个 monitor ，则它将重新进入，并且累加计数。

c. 如果其他线程已经拥有了这个 monitor，那个这个线程就会被阻塞，直到这个 monitor 的计数变成为 0，代表这个 monitor 已经被释放了，于是当前这个线程就会再次尝试获取这个 monitor。

* monitorexit monitorexit 的作用是将 monitor 的计数器减 1，直到减为 0 为止。代表这个 monitor 已经被释放了，已经没有任何线程拥有它了，也就代表着解锁，所以，其他正在等待这个 monitor 的线程，此时便可以再次尝试获取这个 monitor 的所有权。

而实际monitorenter、exit又是依赖Mutex Lock来实现的。而操作系统实现线程之间的切换这就需要从用户态转换到核心态，这个成本非常高。

## 3.Synchronized 常见的坑

1.虽然使用了synchronized关键字，但在修饰实例方法时锁的是实例对象，如果有多个线程创建了多个实例对象去同时执行setX方法，则完全没锁住。解决办法就是把int x 改为 static inx x；
```java
class IncompletedSynchronization {
        int x;

        public int getX() {
            return x;
        }

        public synchronized void setX(int x) {
            this.x = x;
        }
}
```


2.
```shell script
Object lock = new Object();
synchronized (lock) {
        lock = new Object();
}
```

3.使用private final object来作为lock对象。否则如果有恶意，或者无意代码获取该对象的锁并且释放或者直接while(true)循环，则我们永远拿不到锁或提前释放锁。
```shell script
public class SynObject {

    public synchronized  void doSomething(){
        //do something 
    }

    public static void main(String[] args) throws InterruptedException {
        SynObject synObject= new SynObject();
        //模拟恶意代码拿到锁永远不释放
        synchronized (synObject){
            while (true){
                //loop forever
                Thread.sleep(10000);
            }
        }
    }}
```

4.不要synchronize可被重用的对象。而拿到对象就意味着可以拿到对象锁。因为对于Integer和Short来说，如果值的范围在-128 and 127，则属于同一个对象，如果超出了这个范围，则是不同的对象。Boolean、String也有类似的规则。
```shell script
  private final String lock = "lock";
  public void doSomething() {
    synchronized (lock) {
    // ...
  }}
```

参考文档：  
https://developer.aliyun.com/article/775369

## 4.常见面试题
Q:同时访问synchronized的静态和非静态方法，能保证线程安全吗？
A:不能，两者的锁对象不同，前者是类锁（XXX.class）后者是this.

Q:同时访问synchronized方法和非同步方法，能保证线程安全吗？
A:不能，因为synchronized只会对被修饰的方法起作用。

Q:两个线程同时访问两个对象的非静态同步方法能保证线程安全吗？
A：如果锁在对象上，则不能，因为两个对象是两个锁。如果是类锁，所有对象用一把锁，则可以。

Q:synchronized的继承性问题？
A:重写父类的synchronized时，主要分为两种情况。1.子类方法没有被synchronized修饰，由于synchronized不具备继承性，所以子类方法线程不安全。 2.子类方法被synchronized修饰，两个锁对象其实是一把锁，而且是子类对象为锁。

Q:实际代码中怎么使用synchronized？
A:synchronized同步的范围是越小越好。因为若该方法耗时很久，那其它线程必须等到该持锁线程执行完才能运行。所以实际是一般同步代码块。
  而synchronized代码块部分只有这一部分是同步的，其它的照样可以异步执行，提高运行效率。

Q:synchronized是公平锁吗？
A:

Q:选synchronized还是ReentrantLock？
A:ReentrantLock 确实功能更加强大(公平、可定时、可轮询、可中断)，但是危险性也更高，如果忘记在finally中调用unlock则问题会很大。
一般在synchronized 无法满足需求的情况下，ReentrantLock 作为一种高级工具使用。