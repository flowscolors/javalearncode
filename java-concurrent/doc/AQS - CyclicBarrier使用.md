

循环栅栏，等待所有线程完成后才会执行下一步行动。即一组线程相互等待，等所有线程到达屏障点再继续后续操作。
单纯这样看其实和CountDownLatch很像，在CountDownLacth限制数目后，等所有执行完，CountDownLatch减到0，唤醒主线程。
但在CyclicBarrier里就不是这样，而是一堆相同的进程处理某件事，如果涉及到合并，则由最后一个到达的进程完成。
CyclicBarrier 与CountDownLatch区别就是，可以有不止一个栅栏，因为它的栅栏（Barrier）可以重复使用（Cyclic）。  

一个同步器，允许一组线程全部等待彼此到达共同的辅助点。循环屏障在涉及固定大小的线程组程序中很有用，必须偶尔相互等待。
屏障被称为循环是因为它可以在等待线程重新使用后被释放。    

场景：
多接口并发调用。 1.调用harbor接口，查9个helm概览信息。  2.调用查具体信息接口，查9个helm具体信息。  for循环则后面9个循环进行arraylist.add，实际可以并发。
                使用CyclicBarrier所有线程得到返回值后进行arraylist返回,缺点是外调别人接口，无法限制自己的超时时间。


## CyclicBarrier 内部方法
CyclicBarrier内部并不是继承AQS实现的，而是使用ReentrantLock、Condition实现。
```text
1.内部参数

    /** The lock for guarding barrier entry */                同步操作锁
    private final ReentrantLock lock = new ReentrantLock();
    /** Condition to wait on until tripped */                 线程拦截器
    private final Condition trip = lock.newCondition();
    /** The number of parties */                              每次拦截的线程数
    private final int parties;
    /* The command to run when tripped */                     换代前执行的任务
    private final Runnable barrierCommand;
    /** The current generation */                             屏障的当前代
    private Generation generation = new Generation();

    private int count;                                        计数器
            
2.外部调用API
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }                                                 初始化方法，给定参与方线程数、障碍物被触发时执行的给定障碍物动作，由最后一个进入屏障的线程执行
 
    public CyclicBarrier(int parties) {
        this(parties, null);
    }                                                 初始化方法，给定参与方线程数、障碍物被触发时不执行任何操作。

    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }}                                                 await方法，线程调用await表示自己已经到障碍

    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }                                                   await方法，带timeout参数

    private int dowait(boolean timed, long nanos) {···} dowait方法，每次执行count-1，并计较是否为0；0执行指定任务并则唤醒所有线程，进行下一轮操作。

    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }                                                   重置屏障到初始化状态，如果有外部线程await，则会报异常抛出
```

## 特点
一般多线程执行的相同任务，合并操作交给最后一个到达的线程执行。可以用来完成多线程计算数据，最后合并的场景。

在CyclicBarrier类的内部有一个计数器，每个线程在到达屏障点的时候都会调用await方法将自己阻塞，此时计数器会减1，当计数器减为0的时候所有因调用await方法而被阻塞的线程将被唤醒。这就是实现一组线程相互等待的原理，下面我们先看看CyclicBarrier有哪些成员变量。