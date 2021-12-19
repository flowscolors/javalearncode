
Q:synchronized与ReentrantLock的区别？
A: Synchronized是JVM层次的锁实现，ReentrantLock是JDK层次的锁实现；
   Synchronized的锁状态是无法在代码中直接判断的，但是ReentrantLock可以通过ReentrantLock#isLocked判断；
   Synchronized是非公平锁，ReentrantLock是可以是公平也可以是非公平的；
   Synchronized是不可以被中断的，而ReentrantLock#lockInterruptibly方法是可以被中断的；
   在发生异常时Synchronized会自动释放锁（由javac编译时自动实现），而ReentrantLock需要开发者在finally块中显示释放锁；
   ReentrantLock获取锁的形式有多种：如立即返回是否成功的tryLock(),以及等待指定时长的获取，更加灵活；
   Synchronized在特定的情况下对于已经在等待的线程是后来的线程先获得锁（上文有说），而ReentrantLock对于已经在等待的线程一定是先来的线程先获得锁；
   
Q:synchronized的锁升级机制？
A:Java中的synchronized有偏向锁、轻量级锁、重量级锁三种形式，分别对应了锁只被一个线程持有、不同线程交替持有锁、多线程竞争锁三种情况。当条件不满足时，锁会按偏向锁->轻量级锁->重量级锁 的顺序升级。


Q:synchronized的锁能降级吗？
A:在JDK8u中，JVM中的锁是可以降级，只不过条件苛刻。锁降级的代码在deflate_idle_monitors方法中，其调用点在进入SafePoint的方法SafepointSynchronize::begin()中。
在deflate_idle_monitors中会找到已经idle的monitor(也就是重量级锁的对象)，然后调用deflate_monitor方法将其降级。
因为锁降级是发生在safepoint的，所以如果降级时间过长会导致程序一直处于STW的阶段。在这里有篇文章讨论了优化机制。 http://openjdk.java.net/jeps/8183909
jdk8中本身也有个MonitorInUseLists的开关，其影响了寻找idle monitor的方式，对该开关的一些讨论看这里 https://bugs.openjdk.java.net/browse/JDK-8149442

Q:乐观锁和悲观锁的区别？

Q:如何实现一个乐观锁？

Q:介绍一下AQS?
A：AQS是一个提供同步器方法的底层类。
1.AQS维护一个叫做state的int型变量和一个双向链表，state用来表示同步状态，双向链表存储的是等待锁的线程
2.加锁时首先调用tryAcquire尝试获得锁，如果获得锁失败，则将线程插入到双向链表中，并调用LockSupport.park()方法阻塞当前线程。
3.释放锁时调用LockSupport.unpark()唤起链表中的第一个节点的线程。被唤起的线程会重新走一遍竞争锁的流程。
其中tryAcquire方法是抽象方法，具体实现取决于实现类，我们常说的公平锁和非公平锁的区别就在于该方法的实现。

Q:AQS是如何唤醒下一个线程的？
A:AQS中使用

Q:ReentrantLock如何实现公平和非公平锁是如何实现？

Q:CountDownLatch和CyclicBarrier的区别？各自适用于什么场景？

Q:适用ThreadLocal时要注意什么？比如说内存泄漏?

Q:说一说往线程池里提交一个任务会发生什么？

Q:线程池的几个参数如何设置？

Q:线程池的非核心线程什么时候会被释放？

Q:如何排查死锁？

Q:JAVA8的ConcurrentHashMap为什么放弃了分段锁,有什么问题吗,如果你来设计,你如何设计?

Q:画一个线程的生命周期状态图.

