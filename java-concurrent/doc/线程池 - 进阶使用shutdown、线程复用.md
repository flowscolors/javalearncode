

线程池在内部实际上构建了一个生产者消费者模型，将线程和任务两者解耦，并不直接关联，从而良好的缓冲任务，复用线程。所以线程池的运行主要也分为两部分: 
生成者-任务管理。当任务提交后，线程池会判断该任务后续的流转：（1）直接申请线程执行该任务；（2）缓冲到队列中等待线程执行；（3）拒绝该任务。
    private final BlockingQueue<Runnable> workQueue;  使用一个阻塞队列维护线程池中收到的任务。  
消费者-线程管理。它们被统一维护在线程池内，根据任务请求进行线程的分配，当线程执行完任务后则会继续获取新的任务去执行，最终当线程获取不到任务的时候，线程就会被回收。
    private final HashSet<Worker> workers = new HashSet<Worker>(); 使用一个hashset维护现在的持有线程的引用，这些线程会来计算阻塞队列中的任务。

## 线程池总体设计
Java中的线程池核心实现类是ThreadPoolExecutor，本章基于JDK 1.8的源码来分析Java线程池的核心设计与实现。下面是ThreadPoolExecutor的UML类图，

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/Executor-UML.png)

Executor是ThreadPoolExecutor实现的顶层接口，顶层接口Executor提供了一种思想：将任务提交和任务执行进行解耦。用户无需关注如何创建线程，如何调度线程来执行任务，用户只需提供Runnable对象，将任务的运行逻辑提交到执行器(Executor)中，由Executor框架完成线程的调配和任务的执行部分。  
ExecutorService接口增加了一些能力：（1）扩充执行任务的能力，补充可以为一个或一批异步任务生成Future的方法；（2）提供了管控线程池的方法，比如停止线程池的运行。  
AbstractExecutorService则是上层的抽象类，将执行任务的流程串联了起来，保证下层的实现只需关注一个执行任务的方法即可。  
ThreadPoolExecutor作为最下层的实现类实现最复杂的运行部分，ThreadPoolExecutor将会一方面维护自身的生命周期，另一方面同时管理线程和任务，使两者良好的结合从而执行并行任务。

ThreadPoolExecutor内部常用方法:
```text
public class ThreadPoolExecutor extends AbstractExecutorService {
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * Set containing all worker threads in pool. Accessed only when
     * holding mainLock.
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * Wait condition to support awaitTermination
     */
    private final Condition termination = mainLock.newCondition();
}
```

## 线程池运行状态

线程池运行的状态，并不是用户显式设置的，而是伴随着线程池的运行，由内部来维护。线程池内部使用一个变量维护两个值：运行状态(runState)和线程数量 (workerCount)。
在具体实现中，线程池将运行状态(runState)、线程数量 (workerCount)两个关键参数的维护放在了一起，private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

ctl这个AtomicInteger类型，是对线程池的运行状态和线程池中有效线程的数量进行控制的一个字段， 它同时包含两部分的信息：线程池的运行状态 (runState) 和线程池内有效线程的数量 (workerCount)，高3位保存runState，低29位保存workerCount，两个变量之间互不干扰。
用一个变量去存储两个值，可避免在做相关决策时，出现不一致的情况，不必为了维护两者的一致，而占用锁资源。通过阅读线程池源代码也可以发现，经常出现要同时判断线程池运行状态和线程数量的情况。线程池也提供了若干方法去供用户获得线程池当前的运行状态、线程个数。这里都使用的是位运算的方式，相比于基本运算，速度也会快很多。

|  运行状态   | 状态码  | 状态描述 |
|  ----  | ----  | ---- |
| RUNNING  | -1 | 能接受新提交的任务，并且也能处理阻塞队列中的任务 |
| SHUTDOWN  | 0 | 关闭状态，不再提交新提交的任务，但可以继续处理阻塞队列中的任务 |
| STOP  | 1 | 不接受新任务，不处理新提交的任务，且中断正在处理任务的线程 |
| TIDYING  | 2 | 所有任务都已终止，workCount（有效线程数）为0 |
| TERMINATED  | 3 | 在terminated()方法执行完后进入该状态 |

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/threadpool-lifecycle.png)

## Worker内部类
线程池为了掌握线程的状态并维护线程的生命周期，设计了线程池内的工作线程Worker。  
Worker实现了Runnable接口，并且内部有个fianl的Thread，一个初始化任务firstTask。thread是在调用构造方法时通过ThreadFactory创建的线程，可以用来执行任务；firstTask代表一个任务。该任务可以为null，如果非null，则线程会在启动初期执行这个任务，对应coreSize线程创建的逻辑。为null则要去阻塞队列中getTask获取任务。  
Worker继承了AQS，用来把当前Worker是否在运行的状态锁起来，并且是不可重入锁。lock方法一旦获取了独占锁，代表当前线程正在执行任务。

addWorker()方法。实际上addWorker要么就是增加了一个Worker，对应增加了一个线程并执行Worker任务;要么就是往阻塞队列里添加了一个Worker的Runnable。

processWorkerExit()方法。线程池的销毁依赖JVM的GC。从HashSet中remove了就会被GC。线程池就是根据当前线程状态维护一定数目的线程引用即可。

runWork()方法。实际线程池的rund()调用的就是runWork()方法。Worker被创建出来之后就会不断进行轮询，去getTask获取任务去执行，核心线程可以无限等待去获取任务，非核心线程限时获取任务。当Worker无法获取任务，循环结束，Worker主动消除自身在线程池中引用。
1.while循环不断地通过getTask()方法获取任务。 
2.getTask()方法从阻塞队列中取任务。 
3.如果线程池正在停止，那么要保证当前线程是中断状态，否则要保证当前线程不是中断状态。 
4.执行任务。 5.如果getTask结果为null则跳出循环，执行processWorkerExit()方法，销毁线程。

Worker内部类常用方法:
```text
    private final HashSet<Worker> workers = new HashSet<Worker>();

    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

        final Thread thread;                  //该worker中正在运行的线程。如果 ThreadFactory 失败，则为空

        Runnable firstTask;                  //要运行的初始任务。可能为空。
 
        volatile long completedTasks;        //每个线程的任务计数器

        Worker(Runnable firstTask) {
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }                               //使用给定的第一个任务并且任务线程来自 ThreadFactory 创建

        public void run() {
            runWorker(this);
        }                              //将主运行循环委托给外部 runWorker。ThreadPoolExecutor.runWorker方法中会循环

        // Lock methods    锁方法
        // The value 0 represents the unlocked state.  0代表解锁
        // The value 1 represents the locked state.    1代表锁定

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```




## 线程池如何做到线程复用的

1.任务调度  
execute()方法，任务调度方法做为线程池的入口，当用户提交了任务，该任务如何执行都由该阶段决定。表示在未来的某个时间执行给定的任务。进程放入线程池的逻辑,三步:
1. 如果正在运行的线程数少于 corePoolSize，请尝试使用给定命令启动一个新线程作为其第一个任务。对 addWorker 的调用以原子方式检查 runState 和 workerCount，从而通过返回 false 来防止在不应该添加线程时出现误报。
2. 如果一个任务可以成功排队，那么我们仍然需要仔细检查我们是否应该添加一个线程 *（因为自上次检查以来现有线程已死亡）或 * 线程池在进入此方法后关闭.所以我们重新检查状态，并在必要时回滚如果停止，或者如果没有，则启动一个新线程。
3. 如果我们不能排队任务，那么我们尝试添加一个新的线程。如果失败，我们知道我们已经关闭或饱和，因此拒绝该任务。

2.任务缓冲
addworker方法，创建、运行和清理 worker 的方法。检查是否可以根据当前池状态和给定的边界（核心coreSize或最大值maxSize）添加新的 worker。  
1.调整worker数目，如果可能创建并启动一个新的worker，将firstTask做为第一个任务运行。
2.如果线程池已经停止或符合关闭条件，则返回false；如果线程工厂在询问时未能创建线程（如因OOM），则也返回false。
3.当线程数少于 corePoolSize 时（在这种情况下我们总是启动一个线程），或者当队列已满时（在这种情况下我们必须绕过队列）。最初空闲线程通常通过 prestartCoreThread 创建或替换其他垂死的工人。 core 如果为 true，则使用 corePoolSize 作为绑定，否则使用 maximumPoolSize，创建新的线程去绑定。

3.任务申请
getTask方法，任务的执行有两种可能，一是任务直接创建线程执行；二是线程从阻塞队列取出任务，然后执行，执行完任务的空闲线程会再次从队列中申请任务执行。
线程需要从阻塞队列中不断获取任务去执行，帮助线程从阻塞队列中获取任务，实现线程管理模块和任务管理模块之间的通信。
getTask这部分进行了多次判断，为的是控制线程的数量，使其符合线程池的状态。如果线程池现在不应该持有那么多线程，则会返回null值。工作线程Worker会不断接收新任务去执行，而当工作线程Worker接收不到任务的时候，就会开始被回收。

4.各种线程池状态更新的时候使用了ReentrantLock去维护变量的更新。 final ReentrantLock mainLock = this.mainLock;
```text
    public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }

    private boolean addWorker(Runnable firstTask, boolean core) {   //增加线程，不考虑线程池是哪个阶段增加的线程
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            // Are workers subject to culling?
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }
```



## shutdown与shotdownNow

调用shutdown或者shutdownNow，两者都不会接受新的任务，而且通过调用要停止线程的interrupt方法来中断线程，需要注意有可能线程永远不会被中断。  
不同之处在于shutdownNow会首先将线程池的状态设置为STOP，然后尝试停止所有线程（有可能导致部分任务没有执行完）然后返回未执行任务的列表。
而shutdown则只是将线程池的状态设置为shutdown，然后中断所有没有执行任务的线程，并将剩余的任务执行完。

线程池关闭后提交的任务由拒绝策略执行。

5种关闭线程池有关方法
5 种在 ThreadPoolExecutor 中涉及关闭线程池的方法，如下所示。
void shutdown()  可以安全地关闭一个线程池，但是要执行完线程池中现在的任务。   
boolean isShutdown()  返回 true 或者 false 来判断线程池是否已经开始了关闭工作，也就是是否执行了 shutdown 或者 shutdownNow 方法。  
boolean isTerminating()  如果正在中止则返回true。
boolean isTerminated()  可以检测线程池是否真正“终结”了，这不仅代表线程池已关闭，同时代表线程池中的所有任务都已经都执行完毕了    
boolean awaitTermination(long timeout, TimeUnit unit) 调用 awaitTermination 方法后当前线程会尝试等待一段指定的时间，如果在等待时间内，线程池已关闭并且内部的任务都执行完毕了，也就是说线程池真正“终结”了，那么方法就返回 true，否则超时返回 fasle。  
List<Runnable> shutdownNow();  5 种方法里功能最强大的，立刻关闭。在执行 shutdownNow 方法之后，首先会给所有线程池中的线程发送 interrupt 中断信号，尝试中断这些任务的执行，然后会将任务队列中正在等待的所有任务转移到一个 List 中并返回，我们可以根据返回的任务 List 来进行一些补救的操作，例如记录在案并在后期重试。


```text
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }
```