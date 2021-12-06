

ReentrantLock 作为一种锁，可重入互斥与使用synchronized方法和语句包括的代码的锁具有相同的基本行为和语义，但具有拓展的功能。

一个线程来请求ReentrantLock锁：
1.ReentrantLock不属于其他线程，则调用Lock成功，成功获得锁。
2.如果当前线程已经拥有锁，则进行重入流程，Lock方法将立即返回。这时候如果使用线程池，则同一线程是重入直接获取锁的。可以使用方法 {@link #isHeldByCurrentThread} 和 {@link * #getHoldCount} 进行检查。
3.如果ReentrantLock已经被另一个线程拥有则成功锁定，且尚未解锁。
 
ReentrantLock 的构造函数接受一个可选的 fairness 参数。当设置 {@code true} 时，在锁争用下，锁有利于授予对等待时间最长的线程的访问权限。否则这个锁不保证任何特定的访问顺序。
与使用默认设置的程序相比，使用由多个线程访问的公平锁的程序可能会显示较低的总体吞吐量（即，速度较慢；通常慢得多），但获取锁的时间差异较小，并保证不会出现饥饿。但是请注意，锁的公平性并不能保证线程调度的公平性。

因此，使用公平锁的许多线程中的一个可能会连续多次获得它，而其他活动线程没有进展并且当前没有持有锁。 另请注意，未计时的 {@link #tryLock()} 方法不尊重公平性设置。如果锁可用，即使其他线程正在等待，它也会成功。 
推荐的做法是 总是 立即在调用 {@code lock} 之后使用 {@code try} 块，大多数通常在构造之前/之后，例如：

 ```text
class X {
    private final ReentrantLock lock = new ReentrantLock();
    // ...
 
    public void m() {
      lock.lock();  // block until condition holds
      try {
        // ... method body
      } finally {
        lock.unlock()
      }
    }
  }}
```

除了实现 Lock 接口之外，ReentrantLock 还定义了许多 public 和 protected 方法来检查锁的状态。其中一些方法仅对检测和监控有用。 
这个类的序列化与内置锁 synchronized 的行为方式相同：反序列化的锁处于解锁状态，而不管它在序列化时的状态如何。这个锁最多支持同一线程的2147483647个递归锁。尝试超过此限制会Lock方法抛出Error。

## ReentrantLock内部方法
ReentrantLock内部常用方法，注意这里内部有三种Sync，原始Sync、公平的 FairSync、非公平的 NonfairSync:  
* tryRelease 、nonfairTryAcquire 是在父类Sync中定义，实际的FairSync、NonfairSync都只定义了tryAcquire。  
* NonfairSync 的 tryAcquire直接使用父类的nonfairTryAcquire ，Lock 方法则进行了判断后进行 acquire(1)。    
* FairSync 的 tryAcquire 中进行了相关逻辑的重写， Lock 方法则很简单，只是简单的acquire(1) 。
```text
1.Sync、NonfairSync、FairSync继承AQS
    abstract static class Sync extends AbstractQueuedSynchronizer {

        abstract void lock();
    
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {        // 方法返回当前锁是不是没有被线程持有
            int c = getState() - releases;                        // 减少可重入次数
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();        // 当前线程不是持有锁的线程，抛出异常
            boolean free = false;
            if (c == 0) {                                        // 如果持有线程全部释放，将当前独占锁所有线程设置为null，并更新state
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        ···
    }

    static final class NonfairSync extends Sync {
            final void lock() {
                if (compareAndSetState(0, 1))
                    setExclusiveOwnerThread(Thread.currentThread());
                else
                    acquire(1);
            }
    
            protected final boolean tryAcquire(int acquires) {
                return nonfairTryAcquire(acquires);
            }
    }
    
    static final class FairSync extends Sync {
        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

2.ReentrantLock使用

    public ReentrantLock() {
        sync = new NonfairSync();
    }                                      //默认是非公平锁

    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
    
    public void lock() {
        sync.lock();
    }
    
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }
    
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }
    
    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }
    
    public void unlock() {
        sync.release(1);
    }

```


## ReentrantLock特点

|        | ReentrantLock  | Synchronized  |
|  ----  | ----  | ----|
| 锁实现机制  | 依赖AQS | 监视器模型|
| 灵活性  | 支持响应中断、超时、尝试获取锁，支持自己拓展 | 不灵活 |
| 释放形式  | 必须显式调用unLock方法 | 自动释放监视器 |
| 锁类型  | 公平锁&非公平锁 | 非公平锁 |
| 条件队列  | 可关联多个条件队列 | 关联一个条件队列 |
| 可重入性  | 可重入 | 可重入 |

使用方法:
```text
// **************************Synchronized的使用方式**************************
// 1.用于代码块
synchronized (this) {}
// 2.用于对象
synchronized (object) {}
// 3.用于方法
public synchronized void test () {}
// 4.可重入
for (int i = 0; i < 100; i++) {
	synchronized (this) {}
}
// **************************ReentrantLock的使用方式**************************
public void test () throw Exception {
	// 1.初始化选择公平锁、非公平锁
	ReentrantLock lock = new ReentrantLock(true);
	// 2.可用于代码块
	lock.lock();
	try {
		try {
			// 3.支持多种加锁方式，比较灵活; 具有可重入特性
			if(lock.tryLock(100, TimeUnit.MILLISECONDS)){ }
		} finally {
			// 4.手动释放锁
			lock.unlock()
		}
	} finally {
		lock.unlock();
	}
}
```


(●'◡'●)啾咪~♥

## ReentrantLock加解锁方式
1.对于非公平锁
* 若通过CAS设置变量State状态为成功，即获取锁成功，则将当前线程设置为独占线程。 
* 若通过CAS设置变量State状态为失败，即获取锁失败，则进入Acquire(1)方法进行后续处理。

2.对于公平锁
则直接进行Acquire(1)

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/nofair-AQS.png)

加锁：

通过ReentrantLock的加锁方法Lock进行加锁操作。

会调用到内部类Sync的Lock方法，由于Sync#lock是抽象方法，根据ReentrantLock初始化选择的公平锁和非公平锁，执行相关内部类的Lock方法，本质上都会执行AQS的Acquire方法。

AQS的Acquire方法会执行tryAcquire方法，但是由于tryAcquire需要自定义同步器实现，因此执行了ReentrantLock中的tryAcquire方法，由于ReentrantLock是通过公平锁和非公平锁内部类实现的tryAcquire方法，因此会根据锁类型不同，执行不同的tryAcquire。

tryAcquire是获取锁逻辑，获取失败后，会执行框架AQS的后续逻辑(doAcquire -> addWaiter)，跟ReentrantLock自定义同步器无关。

解锁：

通过ReentrantLock的解锁方法Unlock进行解锁。

Unlock会调用内部类Sync的Release方法，该方法继承于AQS。

Release中会调用tryRelease方法，tryRelease需要自定义同步器实现，tryRelease只在ReentrantLock中的Sync实现，因此可以看出，释放锁的过程，并不区分是否为公平锁。

释放成功后，所有处理由AQS框架完成(doRelease)，与自定义同步器无关。


参考文档：  
https://tech.meituan.com/2019/12/05/aqs-theory-and-apply.html