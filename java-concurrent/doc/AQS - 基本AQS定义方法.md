
AQS核心思想:如果被请求的共享资源空闲，那么就将当前请求资源的线程设置为有效的工作线程，并将共享资源设置为锁定状态。
如果共享资源被占用，则需要一定的阻塞唤醒机制来保证锁分配。该机制主要通过变体的CLH实现，将暂时获取不到锁的线程放到队列中。

1.我们可以使用任何同步器去构建其他同步器，比如可以用可重入锁构建信号量，或者反之。然而这样通常是复杂的，开销大的，不灵活的，于是AQS希望作为一个小框架来提供通用机制。

2.同步器有两种方法。1) 至少一种获取阻塞调用线程的操作，除非/直到同步状态允许它继续进行  2) 并且至少有一个以某种方式更改同步状态的释放操作。  
  但是java.util.concurrent 包没有定义一个统一同步器API，有的是接口，有的是方法 Lock.lock，Semaphore.acquire、CountDownLatch.await 和 FutureTask.get
  必要时，每个同步器需要支持：非阻塞同步尝试tryLock、可选超时 让程序放弃等待、通过中断可取消 不可取消 ··· 
  
3.java内置锁synchronized 长期以来一直有性能问题，并且有很多改造文献，但是它们大多关注两个方面。1.尽可能减少空间开销 2.在单线程时减少上下文开销。但是这都不是同步器最关键的问题。   
  1.程序员只在需要使用的使用synchronized 并不会占用太多空间 2.同步器几乎专门为多线程设计，典型的多线程程序都是大量线程使用的。 

4.无论内部制作得多么精巧，同步器都将在某些应用程序中造成性能瓶颈。 因此，该框架必须能够监测和检查基本的操作以允许用户发现和缓解瓶颈。这最少（也是最有用）需要提供一种方法确定有多少线程被阻塞。

5.同步器的设计其实就两点 1)acquire获取锁 while(同步状态不允许获取){尚未排队，则排队；可能阻塞当前线程}  2)release释放锁  更新同步状态，if(状态允许被阻塞线程获取){解除一个或多个阻塞线程}  
  为了实现上述目标，需要一下三个原子组件的配合使用: 以原子方式管理同步状态state、阻塞和解除阻塞线程(tryacquire、acquire)、维护等待队列。


参考论文：  
http://gee.cs.oswego.edu/dl/papers/aqs.pdf

## 1.为何会有AQS

首先让我们思考一下AQS给我们带来了什么，如果没有AQS，这些并发控制器要怎么实现。 **锁，或者协作类的共同点是都有作为阀门的功能。**  
也即如果我想实现信号量、阻塞队列等框架，我发现这里面很多功能(状态标志位的原子性管理、线程的阻塞和解除阻塞、队列的管理)是类似的，那如果能把这些共性实现抽取出来就好了，这就是AQS。  

1.状态标志位  state  
   
   同步状态位，初始值为0，当外部尝试获取同步状态时，如果state为0，且被外部某线程修改成1(或自减)，则认为该线程获取到锁。可以说state就是各个线程来竞争的资源。如果外部没获取到则调用acquire，依靠AQS自身排队机制去获取锁。
   该状态对于不同工具类是不一样的。ReentrantLock 是锁的重入次数；private volatile int state;可以不为1，会根据实现类有不同数目与含义。  
   因为 state 是会被多个线程共享的，会被并发地修改，所以所有去修改 state 的方法都必须要保证 state 是线程安全的。可是 state 本身它仅仅是被 volatile 修饰的，volatile 本身并不足以保证线程安全。  
   可以看出state的调用都很简单粗暴，底层依赖CAS操作。
   
2.线程的阻塞和解除阻塞  tryacquire、tryrelease
  
   这部分对指定线程阻塞、解除阻塞的能力也是共性的。对应锁的获取、释放方法。  
   获取和释放相关的重要方法，这些方法是协作工具类的逻辑的具体体现，需要每一个协作工具类自己去实现，所以在不同的工具类中，它们的实现和含义各不相同。 
   
   
3.队列的管理 CLH  acquire、release

   需要管理排队的线程。FIFO队列，存储等待线程。  
   当多个线程去竞争同一把锁的时候，就需要用排队机制把那些没能拿到锁的线程串在一起；而当前面的线程释放锁之后，这个管理器就会挑选一个合适的线程来尝试抢刚刚释放的那把锁。所以 AQS 就一直在维护这个队列，并把等待的线程都放到队列里面。  
   这个队列内部是双向链表的形式，其数据结构看似简单，但是要想维护成一个线程安全的双向队列却非常复杂，因为要考虑很多的多线程并发问题。
   
   当执行Acquire(1)时，会通过tryAcquire获取锁。在这种情况下，如果获取锁失败，就会在acquire去调用addWaiter加入到等待队列中去。获取锁失败后，会执行addWaiter(Node.EXCLUSIVE)加入等待队列。等待队列中会调用acquireQueued方法进行自旋判断是否获取到锁。
   acquireQueued跳出条件为： “前置节点是头结点，且当前线程获取锁成功”。并且为了防止因死循环导致CPU资源被浪费，我们会判断前置节点的状态来决定是否要将当前线程挂起。
   
   当执行release时，会通过tryRealse去判断锁是否应该释放。如果tryrealse返回true。则判断头结点不为空并且头结点的waitStatus不是初始化节点情况，解除等待队列中线程挂起状态，让队列中线程开始竞争state。
   
那么综上，我们以朴素的价值观得出，"AQS 是一个用于构建锁、同步器等线程协作工具类的框架"这件事。

参考文档:  
第一个资源是 AQS 作者本人 Doug Lea 所写的一篇论文，这篇论文自然是非常宝贵的学习资料，请点击这里查看；  http://gee.cs.oswego.edu/dl/papers/aqs.pdf
第二个是来自 Javadoop 博客对于 AQS 的源码分析的文章，感兴趣的话也可以阅读，请点击这里查看。  https://javadoop.com/post/AbstractQueuedSynchronizer

PS：CLH队列介绍
CLH是一个基于链表（队列）非线程饥饿的自旋（公平）锁，由于是 Craig、Landin 和 Hagersten三人的发明，因此命名为CLH锁。每一个等待锁的线程封装成节点，不断自旋判断前一个节点的状态，如果前一个节点释放锁就结束自旋。

特点：该算法只一个CAS操作，即可让所有等待获取锁的线程构建有序全局队列。

1、首先有一个尾节点指针，通过这个尾结点指针来构建等待线程的逻辑队列（所有每个线程还应该保存前面Node的状态，链表形式），因此能确保线程线程先到先服务的公平性，因此尾指针可以说是构建逻辑队列的桥梁；此外这个尾节点指针是原子引用类型，避免了多线程并发操作的线程安全性问题；

2、每个等待锁的线程在自己的前驱节点某个变量上自旋等待，等待前驱解锁之后即可去获取锁。

参考文档：https://funzzz.fun/2021/05/19/CLH%E9%94%81/

## 2.具体AQS方法
下图中有颜色的为Method，无颜色的为Attribution。

总的来说，AQS框架共分为五层，自上而下由浅入深，从AQS对外暴露的API到底层基础数据。

当有自定义同步器接入时，只需重写第一层所需要的部分方法即可，不需要关注底层具体的实现流程。当自定义同步器进行加锁或者解锁操作时，先经过第一层的API进入AQS内部方法，然后经过第二层进行锁的获取，接着对于获取锁失败的流程，进入第三层和第四层的等待队列处理，而这些处理方式均依赖于第五层的基础数据提供层。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/AQS-methods.png)
```text
0.基本方法
protected AbstractQueuedSynchronizer() { }       //无参构造方法，默认什么都不做

static final class Node {
        
        static final Node SHARED = new Node();   //节点在共享模式下等待的标记，即该Node以共享模式等待锁

        static final Node EXCLUSIVE = null;     //节点在独占模式下等待的标记。即该Node以独占模式等待锁

        static final int CANCELLED =  1;       //指线程已取消获取锁这个请求的waitStatus值。当节点状态变为取消，则会自动从等待队列中释放。

        static final int SIGNAL    = -1;       //指线程已经准备好，就等待资源释放的waitStatus值

        static final int CONDITION = -2;       //指线程正在等待队列中等待条件的waitStatus值，线程需要唤醒

        static final int PROPAGATE = -3;      //指下一个acquireShared应该无条件传播的waitStatus值，仅在线程处于SHARED状态下才被使用
        
        volatile int waitStatus;             //当前节点在队列中的等待状态字段，只接受1，-1，-2，-3，0.0代表非前4种的none情况，初始化默认值。非负意味着节点不需要接受信号
    
        volatile Node prev;                  //链接到当前线程的前驱节点，可以用来检查前驱的waitStatus值
    
        volatile Node next;                  //链接到当前线程的后继节点，可以用来检查后继的waitStatus值

        volatile Thread thread;             //使用这个节点入队的线程，也即该节点对应的任务，构造时初始化，出队时置为null
    
        Node nextWaiter;                    //链接到下一个等待条件的节点。仅独占模式下才能访问到队列
    
        ···
}                         //等待队列中中的节点类，等待队列通常为CLH（自旋锁，这里的CLH是该算法的三个发明人名的缩写，不是阻塞队列）。CLH锁通常为自旋锁，每个节点前驱记录线程信息，用来充当观察器。



1.API层 主要导出方法，子类需要使用或重写这些方法。并且子类无法重写acquire，只能重写tryacquire方法，再通过两者组合实现特定的Lock方法。

    protected boolean tryAcquire(int arg) {
            throw new UnsupportedOperationException();
    }       //尝试以独占模式获取锁。该方法查询对象是否允许以独占模式获取锁，一般如果允许则获取，不允许则排队。该方法总是由来获取锁的线程调用。默认实现直接抛异常。

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }      //尝试将AQS状态设置为以独占模式并发布。该方法总是由执行释放锁的线程调用。默认实现直接抛异常。
    
    
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }    //尝试以共享模式获取锁。该方法查询对象是否允许以共享模式获取锁，一般如果允许则获取，不允许则排队。该方法总是由来获取锁的线程调用。默认实现直接抛异常。
    
    protected boolean tryReleaseShared(int arg) {
         throw new UnsupportedOperationException();
    }    //尝试将AQS状态设置为以共享模式并发布。该方法总是由执行释放锁的线程调用。默认实现直接抛异常。
    
    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }    //如果同步是针对当前（调用）线程以独占方式保持的，则返回 {@code true}。每次调用非等待 {@link ConditionObject} 方法时都会调用此方法。

    public final void acquire(int arg) {
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }   //以独占模式获取锁，忽略中断。 通过至少调用一次tryAcquire实现，成功则返回。否则线程将排队，可能重复阻塞和解除阻塞，调用tryAcquire直到成功。可用来实现 Lock#lock
        //1.tryAcquire失败 2.把对应线程以Node的数据结构addWaiter到双端队列中 3.把该Node进行acquireQueued调用，对排队中的线程进行获取锁操作，该方法会一直去获取锁。 
    
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }  //以独占模式获取锁，如果中断则中止。可用来实现 Lock#lockInterruptibly
    
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquire(arg) ||
            doAcquireNanos(arg, nanosTimeout);
    }  //尝试以独占模式获取，如果中断则中止，如果给定的超时时间已过则失败。通过首先检查中断状态来实现，然后至少调用一次 {@link * #tryAcquire}，成功返回。
       //否则，线程排队，可能重复阻塞和解除阻塞，调用{@link #tryAcquire} 直到成功或线程被中断或超时过去。可用来实现 Lock#tryLock(long, TimeUnit)
    
    public final boolean release(int arg) {
        if (tryRelease(arg)) {                           // 上边自定义的tryRelease如果返回true，说明该锁没有被任何线程持有
            Node h = head;                               // 头结点不为空并且头结点的waitStatus不是初始化节点情况，解除线程挂起状态
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }  //以独占模式释放锁。如果 {@link #tryRelease} 返回 true，则通过解除阻塞一个或多个线程来实现。该方法可用于实现方法{@link Lock#unlock}。

    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }  //在共享模式下获取锁，忽略中断。通过首先调用至少一次 {@link #tryAcquireShared} 实现，成功返回。否则线程将排队，可能重复阻塞和解除阻塞，调用 {@link #tryAcquireShared} 直到成功。
    
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }  //以共享模式释放锁。如果 {@link #tryReleaseShared} 返回 true，则通过解除一个或多个线程的阻塞来实现。 


2.锁获取方法层  
    //各种版本的锁获取的底层程序 
    private void cancelAcquire(Node node) {···}  //取消正在进行的尝试获取锁操作
    
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {···} //检查并更新未能获取的节点的状态。如果线程应该阻塞，则返回 true。这是所有获取循环中的主要信号控制。
    
    static void selfInterrupt() {Thread.currentThread().interrupt();}  //中断当前线程的便捷方法。
    
    private final boolean parkAndCheckInterrupt() {
            LockSupport.park(this);
            return Thread.interrupted();
    }                                           //park然后检查是否中断的便捷方法
    
    //各种版本的锁获取方法
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;                            // 标记是否成功拿到资源
        try {
            boolean interrupted = false;                 // 标记等待过程中是否中断过
            for (;;) {                                   // 开始自旋，要么获取锁，要么中断
                final Node p = node.predecessor();       // 获取当前节点的前驱节点
                if (p == head && tryAcquire(arg)) {      // 如果p是头结点，说明当前节点在真实数据队列的首部，就尝试获取锁（别忘了头结点是虚节点）
                    setHead(node);                       // 获取锁成功，头指针移动到当前node
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())         //parkAndCheckInterrupt主要用于挂起当前线程，阻塞调用栈，返回当前线程的中断状态。
                    interrupted = true;             // 说明p为头节点且当前没有获取到锁（可能是非公平锁被抢占了）或者是p不为头结点，这个时候就要判断当前node是否要被阻塞（被阻塞条件：前驱节点的waitStatus为-1），防止无限循环浪费资源。具体两个方法下面细细分析
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }        //以独占不间断模式获取已在队列中的线程。由条件等待方法以及获取使用。内含for循环自旋去获取锁，直到获取成功或者不再需要获取（中断）。
    
    private void doAcquireInterruptibly(int arg) {···}  //以独占的可中断模式获取。
    
    private boolean doAcquireNanos(int arg, long nanosTimeout){···} //以定时独占模式获取。
    
    private void doAcquireShared(int arg) {···}         //在共享不间断模式下获取
    
    private void doAcquireSharedInterruptibly(int arg){···} //在共享可中断模式下获取
    
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout){···} //在定时共享模式下获取。

3.队列方法层  队列检查方法层，这层已经有部分方法为public，需要提供给子类调用了。

    public final boolean hasQueuedThreads() {return head != tail;}  //查询是否有线程在等待获取。请注意因为由于中断和超时而导致的取消可能会在任何时候发生 true 返回并不能保证任何其他线程将永远获得。
    
    public final boolean hasContended() {return head != null;}      //查询是否有任何线程争用过这个同步器；也就是说，如果一个获取方法曾经被阻塞。
    
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }          //返回队列中的第一个（等待时间最长的）线程，或者返回 null 如果当前没有线程在排队。
    
    private Thread fullGetFirstQueuedThread() {···}               //快速路径失败时调用的 getFirstQueuedThread 版本
    
    public final boolean isQueued(Thread thread) {
        if (thread == null)
            throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread)
                return true;
        return false;
    }        //如果给定线程当前正在排队，则返回 true。 此实现遍历队列以确定给定线程的存在。
    
    final boolean apparentlyFirstQueuedIsExclusive() {···}       //如果明显的第一个排队线程（如果存在）正在以独占模式等待，则返回true 。 如果此方法返回true ，并且当前线程尝试以共享模式获取（即，此方法是从tryAcquireShared调用的），则可以保证当前线程不是第一个排队的线程。 仅用作 ReentrantReadWriteLock 中的启发式方法。
    
    public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
            ((s = h.next) == null || s.thread != Thread.currentThread());
    }      //查询是否有任何线程等待获取比当前线程更长。公平锁加锁时判断等待队列中是否存在有效节点的方法。如果返回False，说明当前线程可以争取共享资源；如果返回True，说明队列中存在有效节点，当前线程必须加入到等待队列中。
    

4.排队方法层  提供对等待队列排队的相关操作，都是private，只允许内部调用。
    
    static final long spinForTimeoutThreshold = 1000L;  //自旋时间，纳秒级别

    private Node enq(final Node node) {
            for (;;) {
                Node t = tail;
                if (t == null) { // Must initialize  //如果没有被初始化，需要进行初始化一个头结点出来。但请注意，初始化的头结点并不是当前线程节点，而是调用了无参构造函数的节点。
                    if (compareAndSetHead(new Node()))  
                        tail = head;
                } else {
                    node.prev = t;
                    if (compareAndSetTail(t, node)) {
                        t.next = node;
                        return t;
                    }
                }
            }
        }                                    //往等待队列中添加一个节点，必要时进行初始化
    
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);   //通过当前的线程和锁模式新建一个节点。
        // Try the fast path of enq; backup to full enq on failure
        Node pred = tail;                                    //Pred指针指向尾节点Tail。
        if (pred != null) {
            node.prev = pred;                                //将New中Node的Prev指针指向Pred。
            if (compareAndSetTail(pred, node)) {             //通过compareAndSetTail方法，完成尾节点的设置。这个方法主要是对tailOffset和Expect进行比较，如果tailOffset的Node和Expect的Node地址是相同的，那么设置Tail的值为Update的值。
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }                                         //往当前线程按给定模式创建节点类，并调用enq加入队列
    
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }                                        //将队列头设置为设置为节点，从而出队。仅由 acquire 方法调用。为了 GC 和抑制不必要的信号和遍历，还清空了未使用的字段。
    
    private void unparkSuccessor(Node node) {···} //唤醒节点的后继节点，如果存在
    
    private void doReleaseShared() {···}    //共享模式的释放操作——信号后继者并确保传播
    
    private void setHeadAndPropagate(Node node, int propagate) {···} //设置队列头，并检查后继者是否在共享模式下等待


5.数据提供层 提供对Node类的操作，以及设置AQS的state。

    private transient volatile Node head;    //等待队列的头部，延迟初始化。除初始化方法，仅setHead方法可以进行修改。

    private transient volatile Node tail;   //等待队列的尾部，延迟初始化。仅通过 via 方法 enq 修改以添加新的等待节点。

    private volatile int state;             //整个AQS同步器的同步状态，用于表示当前临界资源的获锁情况。我们可以通过修改state字段代表的同步状态来实现多线程的独占模式和共享模式。独占state=1，共享state=n。

    protected final int getState() { return state; } //返回同步状态的当前值。 此操作具有 {@code volatile} 读取的内存语义

    protected final void setState(int newState) { state = newState;} //设置同步状态的值。此操作具有 {@code volatile} 写入的内存语义。

    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }  //如果当前状态值等于预期值，则原子地将同步状态设置为给定的更新值。 此操作具有 {@code volatile} 读取 * 和写入的内存语义。
    
6.监控与监测层

    public final int getQueueLength() {···}                    //返回等待获取的线程数的估计值。该值只是一个估计值，因为当此方法遍历内部数据结构时，线程的数量可能会动态变化。这种方法是设计用于监控系统状态，而不是用于同步控制。
    
    public final Collection<Thread> getQueuedThreads() {···}   //返回一个包含可能正在等待获取的线程的集合。因为在构造这个结果时，实际的线程集可能会动态变化，返回的集合只是一个尽力而为的估计。返回集合的元素没有特定的顺序。此方法旨在促进提供更广泛监控设施的子类的构建。
    
    public final Collection<Thread> getExclusiveQueuedThreads() {···} //返回一个包含可能正在等待以独占模式获取的线程的集合。和上一个方法类似，仅估计值，只是这里仅查独占模式的线程。 
    
    public final Collection<Thread> getSharedQueuedThreads() {···} //返回一个包含可能正在等待以共享模式获取的线程的集合。和上一个方法类似，仅估计值，只是这里仅查共享模式的线程。    

7.条件检测基础层 Instrumentation methods for conditions  封装了对Condition、ConditionObject的使用

8.基础支持层 support for instrumentation 封装了一些unsafe方法的调用

```

小Tips:
对于AQS，为什么不使用实现接口，而是子类继承重写?    
因为如果是实现接口的话，那每一个抽象方法都需要实现。比如你把整个 AQS 作为接口，那么需要实现的方法有很多，包括 tryAcquire、tryRelease、tryAcquireShared、tryReleaseShared 等，
但是实际上我们并不是每个方法都需要重写，根据需求的不同，有选择的去实现一部分就足以了，所以就设计为不采用实现接口，而采用继承类并重写方法的形式。  
并且为了让子类一定要重写父类的方法，父类的方法执行完直接跑异常，这样相当于逼迫子类一定要自己实现特定方法。

## 3.使用到AQS的场景
一般自定义同步器需要实现的方法有以下个，也就是API层的5个方法：
方法名	描述
protected boolean isHeldExclusively()	该线程是否正在独占资源。只有用到Condition才需要去实现它。
protected boolean tryAcquire(int arg)	独占方式。arg为获取锁的次数，尝试获取资源，成功则返回True，失败则返回False。
protected boolean tryRelease(int arg)	独占方式。arg为释放锁的次数，尝试释放资源，成功则返回True，失败则返回False。
protected int tryAcquireShared(int arg)	共享方式。arg为获取锁的次数，尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
protected boolean tryReleaseShared(int arg)	共享方式。arg为释放锁的次数，尝试释放资源，如果释放后允许唤醒后续等待结点返回True，否则返回False。

一般来说，自定义同步器要么是独占方式，要么是共享方式，它们也只需实现tryAcquire-tryRelease、tryAcquireShared-tryReleaseShared中的一种即可，并不需要实现全部5个。
AQS也支持自定义同步器同时实现独占和共享两种方式，如ReentrantReadWriteLock。ReentrantLock是独占锁，所以实现了tryAcquire-tryRelease。下面给出一些AQS实现同步器的例子：  

并发包:
ReentrantLock     NonfairSync  使用AQS保存锁重复持有的次数。当一个线程获取锁时，ReentrantLock记录当前获得锁的线程标识，用于检测是否重复获取，以及错误线程试图解锁操作时异常情况的处理。
ReentrantReadWriteLock   NonfairSync  使用AQS同步状态中的16位保存写锁持有的次数，剩下的16位用于保存读锁的持有次数。
Semaphore         Sync  使用AQS同步状态来保存信号量的当前计数。tryRelease会增加计数，acquireShared会减少计数。
CountDownLatch    Sync  使用AQS同步状态来表示计数。计数为0时，所有的Acquire操作（CountDownLatch的await方法）才可以通过。
ThreadPool        Worker  Worker利用AQS同步状态实现对独占线程变量的设置（tryAcquire和tryRelease）。

工具类:
lazyCachedClassRef[interface java.lang.Iterable]
lazyCachedClassRef[class Script1]
GroovyClassValuePreJava7Segment[org.codehaus.groovy.reflection.GroovyClassValuePreJava7]
LockableObject[org.codehaus.groovy.util.LockObject]
Segment[org.codehaus.groovy.util.ManagedConcurrentMap]

使用arthas的vmtool命令可以直观看到AQS的实例。
vmtool --action getInstances --classNmae java.util.concurrent.locks.AbstractQueuedSynchronizer -l -1

曾经使用AQS的类:
FutureTask  已使用voliatile代替AQS存储状态
SynchronousQueue 一种CSP风格的切换
每次自定义同步器接入时，只需要重写第一部分API层所需要的部分方法即可，而不需要关注底层实现的具体流程。  
通常自定义同步器进行加锁解锁操作时，先通过第一层API层进入AQS方法，在第二层进行锁获取操作，如果没有获取到锁，则进行三、四层的等待队列排队处理，队列则需要依赖数据提供层提供队列信息。  


## 4.如何使用AQS自定义同步器
单纯使用AQS，做不了什么，只能类似前人一样来定义一个自己的线程同步工具。

如果想使用 AQS 来写一个自己的线程协作工具类，通常而言是分为以下三步，这也是 JDK 里利用 AQS 类的主要步骤：

第一步，新建一个自己的线程协作工具类，在内部写一个 Sync 类，该 Sync 类继承 AbstractQueuedSynchronizer，即 AQS；
第二步，想好设计的线程协作工具类的协作逻辑，在 Sync 类里，根据是否是独占，来重写对应的方法。如果是独占，则重写 tryAcquire 和 tryRelease 等方法；如果是非独占，则重写 tryAcquireShared 和 tryReleaseShared 等方法；
第三步，在自己的线程协作工具类中，实现获取/释放的相关方法，并在里面调用 AQS 对应的方法，如果是独占则调用 acquire 或 release 等方法，非独占则调用 acquireShared 或 releaseShared 或 acquireSharedInterruptibly 等方法。

Doug lea在AQS的注释源码里就使用了给了两个例子用AQS实现自己的线程协作工具类。AbstractQueuedSynchronizer 类将以上功能并用作“模板方法模式”同步器的基类。子类只定义方法实施控制的状态检查和更新获取和释放。

```java
public class DIYLock  {

    private static class Sync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire (int arg) {
            return compareAndSetState(0, 1);
        }

        @Override
        protected boolean tryRelease (int arg) {
            setState(0);
            return true;
        }

        @Override
        protected boolean isHeldExclusively () {
            return getState() == 1;
        }
    }
    
    private Sync sync = new Sync();
    
    public void lock () {
        sync.acquire(1);
    }
    
    public void unlock () {
        sync.release(1);
    }
}
```
使用上面的自定义的锁就可以实现一定的同步功能了，看下面的一个例子:
```java
public class AQSMain {

    static int count = 0;
    static DIYLock diyLock = new DIYLock();

    public static void main (String[] args) throws InterruptedException {

        Runnable runnable = new Runnable() {
            @Override
            public void run () {
                try {
                    diyLock.lock();
                    for (int i = 0; i < 10000; i++) {
                        count++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    diyLock.unlock();
                }

            }
        };
        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println(count);
    }
}
```
上述代码每次运行结果都会是20000。通过简单的几行代码就能实现同步功能，这就是AQS的强大之处。

参考文档：  
https://tech.meituan.com/2019/12/05/aqs-theory-and-apply.html
