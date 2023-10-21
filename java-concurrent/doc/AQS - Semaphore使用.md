

信号量，用来管理一系列锁，维持一个可获得锁的数目。  
计数信号量，从概念上，信号量维护一组许可。如有必要，每个获取锁的动作都会阻塞，每个释放的动作都会添加一个许可。  

所以实际上Semaphore作为逻辑层并不是真正维护锁，而是计算可用许可的数目并采取相应的行动。只是限制了访问某些资源的线程数，其实并没有实现同步。
也即同一时刻可能有多个线程执行acquire，但是只有指定数目的线程可以执行。Semaphore并不保证同步，它在同一时刻是无法保证同步的，但是却可以保证资源的互斥。

信号量常用于限制线程数访问一些物理或逻辑的资源。源码注释中给了例子。
1.初始化为1的信号量，用来做许可证开关。和其他锁不同的是，该锁可以由一个所有者以外的线程进行释放，因为信号量并没有锁的所有权。这在特殊情况下很有用，例如死锁恢复。  

2.初始化为N的信号量，用来做一系列资源的控制。
比如 数据库连接池，同时进行连接的线程数目有控制，连接不能超过一定数目，连接到达限制数目后，后面线程必须排队等前面线程释放数据库连接。
生活中的饭堂打饭，100个人去三个窗口打饭，Semaphore长度为3，每次每个线程获取acquire(1),获取不到就阻塞，获取则在finally中release。
生活中的停车场停车，100个人去三个车位的停车场，Semaphore长度为3，每次每个线程获取acquire(1),获取不到就阻塞，获取sleep(N)模拟停车，并在finally中release。

3.允许一个获取多个锁的场合。

## Semaphore 内部方法
```
1.Sync继承AQS
Sync(int permits) { setState(permits);}                   初始化方法声明许可的个数

final int getPermits() { return getState();}              子类新增方法，getPermits，实际就是获得AQS的锁个数。

final int nonfairTryAcquireShared(int acquires) {···}     不公平模式获取共享锁，现有锁个数-传参的acquires。

protected final boolean tryReleaseShared(int releases) {···} 释放共享锁，现有锁 = 现有锁 + releases

final void reducePermits(int reductions) {···}             减少证书个数

public int drainPermits() {···}                             获取并返回所有立即可用的许可证，因为获得全部，相当于直接把证书个数减少到0

public int availablePermits() {···}                         返回此信号量中可用的当前许可数


static final class NonfairSync extends Sync {···}          非公平模式的Sync

static final class FairSync extends Sync {···}             公平模式的Sync
            
2.Semaphore 使用
public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }                                                  初始化方法声明Semaphore，输入证书个数，默认非公平模式

public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }                                                 初始化方法声明Semaphore，输入证书个数，输入公平、非公平模式

public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }                                                  acquire获取证书，默认获取一个。在获取到许可之前，该线程将一直阻塞或中断。

public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }                                                  acquire获取证书，默认获取一个。在获取到许可之前，该线程将一直阻塞。

public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }                                                  尝试获取证书。如果没获取到则并不会阻塞，而是继续执行线程其他任务。

public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }                                                   尝试获取证书，带超时时间

public void release() {
        sync.releaseShared(1);
    }                                                   释放证书，默认释放1。释放完给定数目的许可，将其返回到信号量中。

public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }                                                  acquire获取证书，默认获取一个，以上5个方法都加上对应的permits参数

```

## 特点
1.不保证同步，保证互斥。

2.release使用不当的坑。首先我们的release基本是写在finally里，但是当我们程序执行acquire时是可能抛出InterruptException的，此时也会执行finally。导致明明没有获取到许可证的线程，执行了 release 方法，而该方法导致许可证增加。

这就是坑，就是你代码中的 BUG 潜伏地带。而且还非常的危险，你想你代码里面莫名其妙的多了几个“许可证”。就意味着可能又多于你预期的线程在运行。很危险。

也即默认机制可能导致没有获取到许可证的线程，调用了 release 方法。修复办法有：1.catch异常后直接return 2.继承Semaphore并重写acquire release方法。


参考文档：  
https://www.cnblogs.com/crazymakercircle/p/13907012.html
https://mp.weixin.qq.com/s/iObZKas_Xvin-DLG0pQ1Ew