
AQS核心思想:如果被请求的共享资源空闲，那么就将当前请求资源的线程设置为有效的工作线程，并将共享资源设置为锁定状态。如果共享资源被占用，则需要一定的阻塞唤醒机制来保证锁分配。该机制主要通过变体的CLH实现，将暂时获取不到锁的线程放到队列中。

1.我们可以使用任何同步器去构建其他同步器，比如可以用可重入锁构建信号量，或者反之。然而这样通常是复杂的，开销大的，不灵活的，于是AQS希望作为一个小框架来提供通用机制。

2.同步器有两种方法。1) 至少一种获取阻塞调用线程的操作，除非/直到同步状态允许它继续进行  2) 并且至少有一个以某种方式更改同步状态的释放操作。  
  但是java.util.concurrent 包没有定义一个统一同步器API，有的是接口，有的是方法 Lock.lock，Semaphore.acquire、CountDownLatch.await 和 FutureTask.get
  必要时，每个同步器需要支持：非阻塞同步尝试tryLock、可选超时 让程序放弃等待、通过中断可取消 不可取消 ··· 
  
3.java内置锁synchronized 长期以来一直有性能问题，并且有很多改造文献，但是它们大多关注两个方面。1.尽可能减少空间开销 2.在单线程时减少上下文开销。但是这都不是同步器最关键的问题。   
  1.程序员只在需要使用的使用synchronized 并不会占用太多空间 2.同步器几乎专门为多线程设计，典型的多线程程序都是大量线程使用的。 

4.无论内部制作得多么精巧，同步器都将在某些应用程序中造成性能瓶颈。 因此，该框架必须能够监测和检查基本的操作以允许用户发现和缓解瓶颈。这最少（也是最有用）需要提供一种方法确定有多少线程被阻塞。

5.同步器的设计其实就两点 1)acquire获取锁 while(同步状态不允许获取){尚未排队，则排队；可能阻塞当前线程}  2)release释放锁  更新同步状态，if(状态允许被阻塞线程获取){解除一个或多个阻塞线程}  
  为了实现上述目标，需要一下三个原子组件的配合使用: 以原子方式管理同步状态state、阻塞和解除阻塞线程、维护队列。


参考论文：  
http://gee.cs.oswego.edu/dl/papers/aqs.pdf

## 1.为何会有AQS

首先让我们思考一下AQS给我们带来了什么，如果没有AQS，这些并发控制器要怎么实现。 **锁，或者协作类的共同点是都有作为阀门的功能。**  
也即如果我想实现信号量、阻塞队列等框架，我发现这里面很多功能(状态标志位的原子性管理、线程的阻塞和解除阻塞、队列的管理)是类似的，那如果能把这些共性实现抽取出来就好了，这就是AQS。  

1.状态标志位  state  
   
   状态对于不同工具类是不一样的。ReentrantLock 是锁的重入次数；
   状态位，private volatile int state;可以不为1，会根据实现类有不同数目与含义。  
   因为 state 是会被多个线程共享的，会被并发地修改，所以所有去修改 state 的方法都必须要保证 state 是线程安全的。可是 state 本身它仅仅是被 volatile 修饰的，volatile 本身并不足以保证线程安全。  
   可以看出state的调用都很简单粗暴，底层依赖CAS操作。
   
2.线程的阻塞和解除阻塞  
  
   这部分对指定线程阻塞、解除阻塞的能力也是共性的。对应锁的获取、释放方法。  
   获取和释放相关的重要方法，这些方法是协作工具类的逻辑的具体体现，需要每一个协作工具类自己去实现，所以在不同的工具类中，它们的实现和含义各不相同。 
   
   
3.队列的管理

   需要管理排队的线程。FIFO队列，存储等待线程。  
   当多个线程去竞争同一把锁的时候，就需要用排队机制把那些没能拿到锁的线程串在一起；而当前面的线程释放锁之后，这个管理器就会挑选一个合适的线程来尝试抢刚刚释放的那把锁。所以 AQS 就一直在维护这个队列，并把等待的线程都放到队列里面。  
   这个队列内部是双向链表的形式，其数据结构看似简单，但是要想维护成一个线程安全的双向队列却非常复杂，因为要考虑很多的多线程并发问题。
   
那么综上，我们以朴素的价值观得出，"AQS 是一个用于构建锁、同步器等线程协作工具类的框架"这件事。

参考文档:  
第一个资源是 AQS 作者本人 Doug Lea 所写的一篇论文，这篇论文自然是非常宝贵的学习资料，请点击这里查看；  http://gee.cs.oswego.edu/dl/papers/aqs.pdf
第二个是来自 Javadoop 博客对于 AQS 的源码分析的文章，感兴趣的话也可以阅读，请点击这里查看。  https://javadoop.com/post/AbstractQueuedSynchronizer


## 2.如何使用AQS
单纯使用AQS，做不了什么，只能类似前人一样来定义一个自己的线程同步工具。

如果想使用 AQS 来写一个自己的线程协作工具类，通常而言是分为以下三步，这也是 JDK 里利用 AQS 类的主要步骤：

第一步，新建一个自己的线程协作工具类，在内部写一个 Sync 类，该 Sync 类继承 AbstractQueuedSynchronizer，即 AQS；
第二步，想好设计的线程协作工具类的协作逻辑，在 Sync 类里，根据是否是独占，来重写对应的方法。如果是独占，则重写 tryAcquire 和 tryRelease 等方法；如果是非独占，则重写 tryAcquireShared 和 tryReleaseShared 等方法；
第三步，在自己的线程协作工具类中，实现获取/释放的相关方法，并在里面调用 AQS 对应的方法，如果是独占则调用 acquire 或 release 等方法，非独占则调用 acquireShared 或 releaseShared 或 acquireSharedInterruptibly 等方法。

Doug lea在AQS的注释源码里就使用了给了两个例子用AQS实现自己的线程协作工具类。

AbstractQueuedSynchronizer 类将以上功能并用作“模板方法模式”同步器的基类。子类只定义方法实施控制的状态检查和更新获取和释放。

对于AQS，为什么不使用实现接口，而是子类继承重写。因为如果是实现接口的话，那每一个抽象方法都需要实现。比如你把整个 AQS 作为接口，那么需要实现的方法有很多，包括 tryAcquire、tryRelease、tryAcquireShared、tryReleaseShared 等，但是实际上我们并不是每个方法都需要重写，根据需求的不同，有选择的去实现一部分就足以了，所以就设计为不采用实现接口，而采用继承类并重写方法的形式。  

并且为了让子类一定要重写父类的方法，父类的方法执行完直接跑异常，这样相当于逼迫子类一定要自己实现特定方法。

## 3.使用到AQS的场景
并发包:
ReentrantLock     NonfairSync
ReentrantReadWriteLock   NonfairSync  
Semaphore         Sync
CountDownLatch    Sync
ThreadPool        Worker
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

## 4.具体AQS方法
下图中有颜色的为Method，无颜色的为Attribution。

总的来说，AQS框架共分为五层，自上而下由浅入深，从AQS对外暴露的API到底层基础数据。

当有自定义同步器接入时，只需重写第一层所需要的部分方法即可，不需要关注底层具体的实现流程。当自定义同步器进行加锁或者解锁操作时，先经过第一层的API进入AQS内部方法，然后经过第二层进行锁的获取，接着对于获取锁失败的流程，进入第三层和第四层的等待队列处理，而这些处理方式均依赖于第五层的基础数据提供层。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/AQS-methods.png)
```text
1.API层
protected boolean tryAcquire(int arg) {···}   独占模式获取锁
protected boolean tryRelease(int arg) {···}   独占模式释放锁
protected int tryAcquireShared(int arg) {···} 共享模式获取锁
protected boolean tryReleaseShared(int arg) {···} 共享模式释放锁
public final void acquire(int arg) {···}       独占模式忽略中断
public final boolean release(int arg) {···}    独占模式释放
public final void acquireShared(int arg) {···} 共享模式获取
public final boolean releaseShared(int arg) {···} 共享模式释放

2.锁获取方法层
3.队列方法层
4.排队方法层
5.数据提供层
```

每次自定义同步器接入时，只需要重写第一部分API层所需要的部分方法即可，而不需要关注底层实现的具体流程。  
通常自定义同步器进行加锁解锁操作时，先通过第一层API层进入AQS方法，在第二层进行锁获取操作，如果没有获取到锁，则进行三、四层的等待队列排队处理，队列则需要依赖数据提供层提供队列信息。  

