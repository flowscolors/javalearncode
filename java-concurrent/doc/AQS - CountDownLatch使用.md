


计数器，设定一个值，每次-1，减到0时，主线程唤醒。用来保证并发执行的时候等待前置任务完成再开启主线程任务。
同步器，允许一个或多个线程等待直到在其他线程中执行的一组操作完成。注意计数无法重置，需要一个可以重置的计数器，考虑CyclicBarrier。

场景（CountDownLatch源码注释中对以下场景都给了一个例子）：  
1.多个线程调用await等待，直到某个线程完成某个操作后调用release。即用于开关、倒数。
比如:阻止任何工人继续进行的启动信号,直到司机准备好让他们继续.

2.让一个线程调用await等到，等待N个线程完成某个动作或一个线程完成N次动作，每次完成一次调用release。
多接口并发调用。 1.调用harbor接口，查9个helm概览信息。  2.调用查具体信息接口，查9个helm具体信息。  for循环则后面9个循环进行arraylist.add，实际可以并发。
                并且为了防止超时，保证一定时间返回，使用带超时参数的CountDownLatch。该场景使用CyclicBarrier、fork-join-pool也可以。
                注意这种并发操作还要考虑httpclient连接池的多并发操作。
                
## CountDownLatch 内部方法
```text
1.Sync继承AQS
Sync(int count) { setState(count);}                    初始化方法声明锁的个数

protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }                                              重写共享模式获取锁，只用当state=0才返回1，可以获取到。否则进阻塞队列，这样当state=0，所有阻塞的方法都被可以执行。

protected boolean tryReleaseShared(int releases) {
 // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
}}                                                     重写共享模式释放锁，实际传参release并没有被使用，每次执行state-1操作。
            
2.CountDownLatch使用
public CountDownLatch(int count) {···}                 初始化方法声明CountDownLatch，实际主要就是初始化Sync

public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }                                                  await方法，每次去sync中获取一个锁，实际是进入阻塞队列。

public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
}                                                      await方法，带timeout参数，每次去带超时时间的获取一个锁

public void countDown() {
        sync.releaseShared(1);
    }                                                  countDown方法，每次去释放一个锁
```

## 特点
使用简单，核心接口只有4个，初始化、await、带timeout的await、countdown。就可以实现多await一和一await多的功能。

多await一:  
多个任务被一个任务唤醒的场景

一await多:  
一般定义一个线程池，线程池的execute中执行同一个方法只是参数不同，每次执行完的finallly中执行countdown。
这样当所有的任务借助线程池执行完时，主线程任务就可以开启了，主线程await视情况开启timeout。

两个CountDownLatch的使用:  
两个CountDownLatch同步器互相影响实现功能。
 *   private final CountDownLatch startSignal;
 *   private final CountDownLatch doneSignal;
 

参考文档：  
https://www.cnblogs.com/crazymakercircle/p/13906922.html