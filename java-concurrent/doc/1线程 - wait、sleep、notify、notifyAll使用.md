## 1.wait/notify/notifyAll介绍
### wait
void wait()	Causes the current thread to wait until another thread invokes the notify() method or the notifyAll() method for this object.
void wait(long timeout)	Causes the current thread to wait until either another thread invokes the notify() method or the notifyAll() method for this object, or a specified amount of time has elapsed.
void wait(long timeout, int nanos)	Causes the current thread to wait until another thread invokes the notify() method or the notifyAll() method for this object, or some other thread interrupts the current thread, or a certain amount of real time has elapsed.

JDK中一共提供了这三个版本的方法，

　　（1）wait()方法的作用是将当前运行的线程挂起（即让其进入阻塞状态），直到notify或notifyAll方法来唤醒线程.所以wait方法一定要有对应监视器的锁，即Synchronized。

　　（2）wait(long timeout)，该方法与wait()方法类似，唯一的区别就是在指定时间内，如果没有notify或notifAll方法的唤醒，也会自动唤醒。

　　（3）至于wait(long timeout,long nanos)，本意在于更精确的控制调度时间，不过从目前版本来看，该方法貌似没有完整的实现该功能，对纳秒的处理依旧是四舍五入，所以还是按照毫秒级来处理。


### notify/notifyAll
void notify()	Wakes up a single thread that is waiting on this object's monitor.
void notifyAll()	Wakes up all threads that are waiting on this object's monitor.
　　有了对wait方法原理的理解，notify方法和notifyAll方法就很容易理解了。既然wait方式是通过对象的monitor对象来实现的，所以只要在同一对象上去调用notify/notifyAll方法，就可以唤醒对应对象monitor上等待的线程了。notify和notifyAll的区别在于前者只能唤醒monitor上的一个线程，对其他线程没有影响，而notifyAll则唤醒所有的线程，
　　最后，有两点点需要注意：

　　（1）调用wait方法后，线程是会释放对monitor对象的所有权的。

　　（2）一个通过wait方法阻塞的线程，必须同时满足以下两个条件才能被真正执行：

　　　　线程需要被唤醒（超时唤醒或调用notify/notifyll）。
　　　　线程唤醒后需要竞争到锁（monitor）。
    这也是后面在实现阻塞队列时，如果多线程进行消费，哪怕都被唤醒了，但是只有一个能竞争到锁，进行消费。其余没竞争到的应该继续执行阻塞的逻辑。  
   

## 2.wait 使用场景
要求必须在同步代码块中执行。
也就是在说,就是需要在调用wait()或者notify()之前，必须使用synchronized语义绑定住被wait/notify的对象。
否则会有报错。
```
Exception in thread "main" java.lang.IllegalMonitorStateException
	at java.lang.Object.wait(Native Method)
	at java.lang.Object.wait(Object.java:502)
	at Thread.ThreadStatusDemo.main(ThreadStatusDemo.java:38)
```
waiting code

## 3.由基于wait notify的阻塞队列、生产者-消费者展开

waiting code
已完成，待优化
leetcode/MyBlockingQueue_Wait.java:16
leetcode/ProCon_ObjectWait.java:5

## 4.wait/notify 和 sleep 方法的异同
上面一组线程协作(wait/notify/notifyall)都在Object中,需要拿对象头中的锁。
还有一组线程协作(sleep/yield/join)它们都在Thread中，因为它们只管线程调度，不影响真正的锁。

　1、sleep

　　sleep方法的作用是让当前线程暂停指定的时间（毫秒），sleep方法是最简单的方法。唯一需要注意的是其与wait方法的区别。最简单的区别是，wait方法依赖于同步synchronized，而sleep方法可以直接调用，但是需要try catch。  
而更深层次的区别在于sleep方法只是暂时让出CPU的执行权，并不释放锁，那也就不必获取锁了。而wait方法则需要释放锁。

2、yield方法
　　yield方法的作用是暂停当前线程，以便其他线程有机会执行，不过不能指定暂停的时间，并且也不能保证当前线程马上停止。yield方法只是将Running状态转变为Runnable状态。
   源码注释中有提醒：
 * 调度器可能会忽略该方法。
 * 使用的时候要仔细分析和测试，确保能达到预期的效果。
 * 很少有场景要用到该方法，主要使用的地方是调试和测试。
 
 3、join方法
 
 void join()	Waits for this thread to die.
 void join(long millis)	Waits at most millis milliseconds for this thread to die.
 void join(long millis, int nanos)	Waits at most millis milliseconds plus nanos nanoseconds for this thread to die.
 　　join方法的作用是父线程等待子线程执行完成后再执行，换句话说就是将异步执行的线程合并为同步的线程。JDK中提供三个版本的join方法，其实现与wait方法类似，join()方法实际上执行的join(0)，而join(long millis, int nanos)也与wait(long millis, int nanos)的实现方式一致，暂时对纳秒的支持也是不完整的。
    实际join方法的使用场景会更多，JDK 1.8的forkjoinPool就是基于此。  
    join方法就是通过wait方法来将线程的阻塞，如果join的线程还在执行，则将当前线程阻塞起来，直到join的线程执行完成，当前线程才能执行。不过有一点需要注意，这里的join只调用了wait方法，却没有对应的notify方法，原因是Thread的start方法中做了相应的处理，所以当join的线程执行完成以后，会自动唤醒主线程继续往下执行。



参考文档： 
https://www.cnblogs.com/paddix/p/5381958.html




