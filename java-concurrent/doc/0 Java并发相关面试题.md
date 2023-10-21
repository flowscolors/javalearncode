
Q:synchronized与ReentrantLock的区别？
A: Synchronized是JVM层次的锁实现，ReentrantLock是JDK层次的锁实现；
   Synchronized的锁状态是无法在代码中直接判断的，但是ReentrantLock可以通过ReentrantLock#isLocked判断；
   Synchronized是非公平锁，ReentrantLock是可以是公平也可以是非公平的；
   Synchronized是不可以被中断的，而ReentrantLock#lockInterruptibly方法是可以被中断的；
   在发生异常时Synchronized会自动释放锁（由javac编译时自动实现），而ReentrantLock需要开发者在finally块中显示释放锁；
   ReentrantLock获取锁的形式有多种：如立即返回是否成功的tryLock(),以及等待指定时长的获取，更加灵活；
   Synchronized在特定的情况下对于已经在等待的线程是后来的线程先获得锁（上文有说），而ReentrantLock对于已经在等待的线程一定是先来的线程先获得锁；
   ReentantLock使用时会创建很多对象，可能会导致占用内存过多。而synchronized由于使用的是对象头来置位，所以其实空间占用不多。

PS:类似synchronized与ReentrantLock的区别是使用JVM内置锁MutexObject和AQS的区别。还有一组使用VM内置锁MutexObject和AQS实现的组件，那就是Condition和Object.wait/notify
这组组件是用来实现线程间通信和协作机制的组件，两者的功能都是使得一个线程等待某个条件。两者都是“等待-通知”方法。

Condition类的await方法和Object类的wait方法等效。
Condition类的signal方法和Object类的notify方法等效。
Condition类的signalAll方法和Object类的notifyAll方法等效。

不过由于Condition对象是基于显示锁的，所以不能独立创建Condition对象，而是需要借助显示锁实例去获取其绑定的Conditin对象，即lock.newCondition()。


Q:synchronized的锁升级机制？
A:Java中的synchronized有偏向锁、轻量级锁、重量级锁三种形式，分别对应了锁只被一个线程持有、不同线程交替持有锁、多线程竞争锁三种情况。当条件不满足时，锁会按偏向锁->轻量级锁->重量级锁 的顺序升级。

PS:对于ReentrantReadWriteLock中也有锁升级、降级。对于读写锁读锁可以升级为写锁，写锁可以降级为读锁。ReentrantReadWriteLock不支持升级，只支持降级。
对于StampedLock可以认为是ReentrantReadWriteLock的一个改进。支持三种读写模式。

Q:synchronized的锁能降级吗？
A:在JDK8u中，JVM中的锁是可以降级，只不过条件苛刻。锁降级的代码在deflate_idle_monitors方法中，其调用点在进入SafePoint的方法SafepointSynchronize::begin()中。
在deflate_idle_monitors中会找到已经idle的monitor(也就是重量级锁的对象)，然后调用deflate_monitor方法将其降级。
因为锁降级是发生在safepoint的，所以如果降级时间过长会导致程序一直处于STW的阶段。在这里有篇文章讨论了优化机制。 http://openjdk.java.net/jeps/8183909
jdk8中本身也有个MonitorInUseLists的开关，其影响了寻找idle monitor的方式，对该开关的一些讨论看这里 https://bugs.openjdk.java.net/browse/JDK-8149442

Q:可中断锁和不可中断锁？
A:在抢锁过程中如果能通过某些方法终止抢占过程，这就是可中断锁，否则就是不可中断锁。
如果某一线程A正在占有锁在执行临界区代码，另一线程在阻塞式抢占锁。如果线程B不想等待，可以自己中断自己的阻塞等待，则是可中断锁。
ReentrantLock是可中断锁，Synchronized是不可中断锁。

Q:乐观锁和悲观锁的区别？
A:以线程进入临界区前是否锁住同步资源的角度可以把锁分为悲观锁和乐观锁。
悲观锁认为每次进入临界区的操作都可能被别的线程修改，所以线程每次都会实际上锁，其他线程读写该数据时会阻塞。一般来说悲观锁适用于写多读少的情况，高并发写时性能较高。
乐观锁认为每次进入临界区的操作不会被别的线程修改，所以不会上锁，而是在更新的时候去判断下别人有没有更新数据，有的话就失败，无的话就更新。适用读多写少的情况。
Java里的乐观锁都大多基于CAS自旋实现，且synchronized的重量级锁是悲观锁，而偏向锁和轻量级锁是乐观锁。
虽然在竞争激烈的情况下乐观锁性能会比较差，但是JUC的显示锁Lock底层却是CAS，性能却很好，原因在于AQS通过队列很大程度上较少锁争用，减少了CAS空的自旋。

Q:如何实现一个乐观锁？
A:Java中的乐观锁大多是通过CAS+自旋实现，再往上加可以有版本号、多变量组合、排队队列来提高该机制的性能。主要操作就是冲突检测+数据更新。

Q:悲观锁、乐观锁、自旋锁的使用场景、实现方式、优缺点
悲观锁，一层层最后需要用到OS级别的pthread_mutex_lock。synchronized的重量级锁是悲观锁，而偏向锁和轻量级锁是乐观锁。
乐观锁，基于CAS命令实现的比较，一般需要进行自旋操作。
自旋锁，一般是while+CAS实现。由于CPU空旋回浪费大量CPU，且在SMP架构的CPU上会产生总线风暴。一般需要使用一些优化手段，如空间换时间的分散操作热点和使用队列削峰，AQS就是自旋锁+队列。

Q:synchronized和lock和LockSupport
synchronized，JVM内置锁，使用方法可以锁类、锁方法、锁代码块，不需要手工解锁。有锁升级机制，底层实现基于pthread_mutex_lock.
lock，接口，仅5个方法，Java语言级别的锁，基于AQS+volitale关键字实现，需要手工进行解锁。可以进行超时解锁、重入/不重入、公平/非公平的自定义。一些分布式锁也实现了该接口，方便使用。
LockSupport，JUC提供的一个线程阻塞和唤醒的工具类，可以让线程在任意地方阻塞和唤醒，所有方法都是静态方法。主要方法是LockSupport.park()、LockSupport.unpark()。类似Thread.sleep()、Object.wait()。
AQS的唤醒后继节点最后就是执行LockSupport.unpark()，LockSupport本身不是Native方法，不过park是调用了一个Unsafe类的Native方法。

Q:介绍一下AQS?
A：AQS是一个提供同步器方法的底层类。AQS是JUC提供的一个用于构建锁和同步容器的基础类，AQS是CLH队列的一个变种，维护一个FIFO的双向链表，每个节点都是由线程封装。
当线程争抢锁失败之后就会进入被封装成节点加入AQS队列，而当拥有锁的线程释放锁后，会从队列中唤醒一个阻塞的节点（线程）。

1.AQS维护一个叫做state的int型变量和一个双向链表，state用来表示同步状态，双向链表存储的是等待锁的线程
2.加锁时首先调用tryAcquire尝试获得锁，如果获得锁失败，则将线程插入到双向链表中，并调用LockSupport.park()方法阻塞当前线程。
3.释放锁时调用LockSupport.unpark()唤起链表中的第一个节点的线程。被唤起的线程会重新走一遍竞争锁的流程。
其中tryAcquire方法是抽象方法，具体实现取决于实现类，我们常说的公平锁和非公平锁的区别就在于该方法的实现。

Q:AQS是如何唤醒下一个线程的？
A:AQS中获取锁失败的线程会被封装成节点，addWaiter()，之后自旋执行acquireQueued()方法（代码上是while死循环自旋，实际会park挂起）启动自旋抢锁流程，注意该方法中也有自旋死循环。
当前节点会不断在前驱节点上进行普通自旋，当前驱节点是头节点时，执行抢锁逻辑。相当于头节点获取了锁，该节点绑定的线程会终止acquireQueued()自旋，线程执行临界区代码。
临界区代码执行完lock.unlock()之后会去唤醒后继节点，公平/非公平就是唤醒下一个还是唤醒所有的区别。

Q:ReentrantLock如何实现公平和非公平锁是如何实现？
A:ReentrantLock把所有Lock接口委派到一个Sync类。Sync类是AQS的一个子类，所以本质是ReentrantLock是靠AQS实现。


Q:reentrantlock的实现原理，加锁和释放锁的一个过程，aqs，公平和非公平，可重入，可中断怎么实现的
A:ReentrantLock把所有Lock接口委派到一个Sync类。Sync类是AQS的一个子类，所以本质是ReentrantLock是靠AQS实现。
加锁lock()方法，会调用AQS的acquire()方法，acquire()保证至少执行一次tryAcquire()方法，如果尝试成功则认为抢到锁，acquire()直接返回。
如果tryAcquire()方法失败，没抢到锁，则构造同步节点，通过addWaiter()方法将该节点加入同步队列队尾。
节点入队后，执行acqureQueued()，启动自旋抢锁流程：当前节点会在死循环中不断获取同步状态，不断在前驱节点上自旋，会被挂起Park进入阻塞状态，只有当前驱节点是头节点的时候才会在自旋中获取锁。

释放锁unlock()方法，会调用AQS的release()方法，release()方法中会调用tryRelease()方法，调用成功则执行unparkSuccessor()方法，该方法会唤醒后继节点 Node s = node.next ; LockSupport.unpark(s.thread);。
如果执行tryRelease()失败，则会返回false，release()失败。

----
Q:了解哪些并发工具类
Semaphore 共享锁，在同一时刻允许多个线程持有的锁。semaphore.acquire()、semaphore.release()
CountDownLatch 共享锁，相当于一个多线程环境下的倒数器。
CyclicBarrier

Q:CountDownLatch和CyclicBarrier的区别？各自适用于什么场景？
A:区别在于CyclicBarrier可以做多次等待启动，每次完成规定任务再一起进行下一次任务。

Q:concurrenthashmap原理，put，get，size，扩容，怎么保证线程安全的，1.7和1.8的区别，为什么用synchronized，分段锁有什么问题，hash算法做了哪些优化
JDK1.8已经抛弃了分段锁，而是使用了数组+链表/红黑树的组合方式，使用CAS + Synchronized来保证并发更新的安全。但是思路是一致的。
JDK1.7中默认把一个table分裂成16个小的table（Segment），在Segment进行细粒度的并发控制。实际上并发线程过多的时候粒度还是不够，JDK1.8进行更一步细化，将并发粒度细化到每一个桶里。
每次访问只对一个桶进行锁定，而不对整个Map进行粗粒度锁定。每次put方法中，对桶的对象进行加锁 f = tabAt（···） ； synchronized(f) {···};

Q:JAVA8的ConcurrentHashMap为什么放弃了分段锁,有什么问题吗,如果你来设计,你如何设计?
A:16段还是不够应对高并发。

Q:适用ThreadLocal时要注意什么？比如说内存泄漏?
A:不再使用ThreadLocal对象时记得remove。

Q:threadlocal用过么，什么场景下使用的，原理，hash冲突怎么办，扩容实现，会有线程安全问题么，内存泄漏产生原因，怎么解决
A：存请求的cooikes或者body，或者数据库连接池存session。原理就是每个线程都由ThreadLocalMaps,仅由ThreadLocal进行操作。hash冲突则线性探测。扩容靠数组。
某种程度上是线程安全的，内存泄漏是因为value没有弱引用，所以如果没有remove不会被删除。

Q:为什么ThreadLocal使用弱引用？
A:虽然ThreadLocal是设置成弱引用，但其实堆栈中线程执行的时候是存在对threadlocal的强引用。而当线程任务执行完，就结束了对threadlocal的强引用，这时候说明thredlocal就没用了。  
但是此时threadlocalmap中还是有key指向它，如果是默认的强引用，则无法进行GC。而使用弱引用，相当于让没用的threadlocal在下一次GC时被回收。

参考文档：  
https://mp.weixin.qq.com/s/7IijM60IVFMbr1WA3vhT-w

Q:AtomicInteger,原理是什么,如何做到高效率的，有什么优化措施
A:基于CAS + valatile 实现，同级的由AQS、非阻塞结构。用以构建更上层的显示锁、同步工具类、阻塞队列、线程池、并发容器。底层就是Unsafe的


---
Q:说一说往线程池里提交一个任务会发生什么？
A:coreSize -> BlockingQueue -> maxSize

Q:线程池的几个参数如何设置？
coreSixe
maxSize
BlockingQueue
ThreadFactory
RejectExecuteionHandler
KeepaliveTime
TimeUnit

Q:线程池的非核心线程什么时候会被释放？
A:任务执行完成后，经过keepaliveTime之后被释放。

Q:阻塞队列的用途、区别
A:阻塞队列和普通队列的最大不同点在于阻塞队列提供了阻塞式的添加和删除方法。阻塞的添加和删除，是当队列满时添加和队列空时删除都会被阻塞，直到满足时再唤醒操作线程。
阻塞队列可以很容易实现多线程之间的数据共享和通信。一个线程添加，一个线程阻塞。

Q:LinkedBlockingQueue对列的add、put区别，实际过程中如何使用
A:add添加成功返回true，否则抛出异常。put添加成功则返回，队列已满则阻塞。实际一般在线程池中被使用。

Q:线程有哪些状态，等待状态怎么产生，死锁状态的变化过程，中止状态，interrupt()方法
New  已创建，但未start()。
Runnable  调用start()，Java中把Ready(就绪)和Running（执行）合并成了一个状态。
Blocked   阻塞状态，线程等待锁、发起阻塞IO操作都会进入阻塞状态。
Waiting   无限期等待状态，Object.wait()、Thread.join()、LockSupport.park()方法会进入该状态。
Time-Waiting  有限期等待状态，Thread.sleep(time)、Object.wait(time)、LockSupport.parkNanos(time)方法
Termiated   线程任务完成之后，正常会进入Termiated状态。执行中抛出异常也会进入此状态。

Q:画一个线程的生命周期状态图.阻塞的状态有哪几种,运行顺序
New  已创建，但未start()。
Runnable  调用start()，Java中把Ready(就绪)和Running（执行）合并成了一个状态。
Blocked   阻塞状态，线程等待锁、发起阻塞IO操作都会进入阻塞状态。
Waiting   无限期等待状态，Object.wait()、Thread.join()、LockSupport.park()方法会进入该状态。
Time-Waiting  有限期等待状态，Thread.sleep(time)、Object.wait(time)、LockSupport.parkNanos(time)方法
Termiated   线程任务完成之后，正常会进入Termiated状态。执行中抛出异常也会进入此状态。

Q:while(true)里面一直new thread().start()会有什么问题
A:会OOM。

Q:你怎么理解线程安全，哪些场景会产生线程安全问题，有什么解决办法
A:线程安全就是多线程的操作下不会产生预期之外的相互影响。最好的办法就是不要做线程共享变量，每个线程做自己的事就好，每个线程是无状态的。
比如对arraylist、hashmap的读操作就不会产生线程问题，多线程读也不会。比如外调10个接口得到返回值，如果我只是post则直接发就可以，但是如果我要拿到结果并存起来，那就需要考虑多线程和线程安全了。
而经典的多线程累加就更是一个会产生线程安全的问题了。

Q:如何排查死锁？  
A:jstack、jamp -histo | grep AQS、arthas 。

Q:高并发设计模式？
线程安全的单例模式。 双检锁 + valitile
Fork-Join模式。 分而治之，二分查找、快速排序、ForkJoinPool、Hadoop ··· 适合CPU密集型任务
生产者-消费者模式。 阻塞队列、消息中间件。
Master-Work模式。 Nginx中被使用，计存分离，Reactor模式就是此模式的体现。
Future模式。
异步回调模式。FutureTask、CompleteFuture、Guava的异步回调、Netty的异步回调、RxJava。

Q:介绍下Java内存模型？
JMM更多体现为一种规范和规则，最初由JSR-133文档描述。该规范定义了一个线程对共享变量写入时，如何确保对另一个线程是可见的。从而在语言级别解决可见性和有序性。
并且JMM的另一价值是在能够屏蔽各种硬件和操作系统的访问差距，保证Java程序在各种平台下对内存的访问最终都是一致的。具体如下：
1）所有变量存储在主存中。
2）每个线程都有自己的工作内存，且对变量的操作都是在工作内存中进行。
3）不同线程之间无法直接访问彼此工作内存中的变量，想要访问只能通过主存来传递。
为此JMM提供了一系列规则（如happen-before）、一系列内存操作指令集，并封装了volatile、synchronized等关键字来实现该规范。



