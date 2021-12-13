
## 无锁->偏向锁->轻量级锁->重量级锁

之前是每次都要竞争，竞争不到的都被操作系统阻塞了，这涉及用户态、内核态切换。  
现在有一个膨胀过程，并且少量会走到重量级锁。
现在有一个膨胀过程，并且少量会走到重量级锁。

## 3.锁膨胀
HotSpot作者发现的“大多数锁只会由同一线程并发申请”,于是在轻量竞争下直接自旋可能是更好的选择。
于是设计了无锁 ->偏向锁->轻量级锁->重量级锁。JDK5中出现，JDK6中完善。Synchronized 在1.6 之前只是重量级锁。

在1.6之前的重量级锁，会有线程的唤醒和阻塞，该操作借助操作系统的系统调用使用，linux下就是利用pthread的mutex实现。系统调用就涉及用户态、内核态切换了。 



|  锁   | 触发情况  | 优点 | 缺点 | 适用场景 | 
|  ----  | ----  | ----| ----| ----|
| 单元格  | 单元格 | | |  |
| 单元格  | 单元格 | | |  |
| 单元格  | 单元格 | | |  |
| 单元格  | 单元格 | | |  |

## C++源码
这里相关的几个文件目录为(hpp为HotSpot虚拟机源码)，需要注意不同小版本的源码也会有差异，这里是使用jdk8-b-102：

ObjectMonitor 对象结构体定义在 /openjdk/hotspot/src/share/vm/runtime/objectMonitor.hpp

ObjectMonitor enter、exit等方法定义在 /openjdk/hotspot/src/share/vm/runtime/objectMonitor.cpp

InterpreterRuntime  monitorenter、monitorexit等方法定义在 src/share/vm/interpreter/interpreterRuntime.cpp

Monitor lock、unlock的相关方法定义在 /openjdk/hotspot/src/share/vm/runtime/mutex.hpp

### ObjectMonitor
以下为简单介绍:
```text
  ObjectMonitor() {
    _header       = NULL;
    _count        = 0;
    _waiters      = 0,
    _recursions   = 0;        //线程重入次数
    _object       = NULL;     //存储该monitor的对象
    _owner        = NULL;     //标识拥有该monitor的线程
    _WaitSet      = NULL;     //处于wait状态的线程会被加入waitSet
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;    //多线程竞争锁时的单向列表
    FreeNext      = NULL ;
    _EntryList    = NULL ;    //处于等待锁block状态的线程，会被加入该列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```

* _owner: 初始时为NULL。当有线程占有该monitor时，owner标记为该线程的唯一标识。当线程释放monitor时，owner又恢复为NULL。owner是一个临界资源，JVM是通过CAS操作来保证其线程安全。
* _cxq: 竞争队列，所有请求锁的线程首先会被放在这个队列中(单向列表)。cxq是一个临界资源，JVM通过CAS原子指令来修改cxq队列。修改前cxq的旧值填入了node的next字段，cxq指向新值(新线程)。因此cxq是一个后进先出的stack（栈）。
* _EntryList：cxq队列中有资格成为候选资源的线程会被移动到该队列中。
* _WaitSet：因为调用wait方法而被阻塞的线程会被放在该队列中。

每一个Java对象都可以与一个监视器monitor关联，我们可以把它理解成为一把锁，当一个线程想要执行一段被synchronized圈起来的同步方法或者代码块时，该线程得先获取到synchronized修饰的对象对应的monitor。

我们的Java代码里不会显示地去创造这么一个monitor对象，我们也无需创建，事实上可以这么理解： monitor并不是随着对象创建而创建的。我们是通过synchronized修饰符告诉JVM需要为我们的某个对象创建关联的monitor对象。每个线程都存在两个ObjectMonitor对象列表，分别为free和used列表。 同时JVM中也维护着global locklist。当线程需要ObjectMonitor对象时，首先从线程自身的free表中申请，若存在则使用若不存在则从global list中申请。

因此我们也可以使用ObjectMonitor实现不同的功能：

1.同步。synchronized。此时Monitor相当于一个许可证，任何线程想要访问临界资源，都必须获得这个许可证，并在访问完临界资源后，归还这个许可证。 _cxq

2.协作。wait notify。Monitor也同时提供协作的机制，正在执行同步代码的线程可以释放掉自己获得的“许可证”并进行休眠，而其他线程可以再次获得“许可证”并唤醒线程，从而实现线程协作。 _WaitSet

ObjectMonitor的数据结构、以及关系：



Java中synchronized的同步语义主要基于Java对象头和monitor实现，每个monitor同一时间只能由一个线程拥有，其他想要获取该monitor只能等待

其中monitor具有两个队列：WaitSet 和 EntryList。当某个线程在拥有monitor时，调用了Object.wait()，则会释放monitor，进入WaitSet队列等待，此时线程状态为WAITING，WaitSet中的等待的状态是 “in Object.wait()”。

当其他线程调用Object的notify()/notifyAll()唤醒该线程后，将会重新竞争monitor；当某个线程尝试进入synchronized代码块或方法时，获取monitor失败则会进入EntryList队列，此时线程状态为BLOCKED，EntryList中等待的状态为“waiting for monitor entry”。

### monitor竞争
1.执行monitorenter时首先调用InterpreterRuntime::monitorenter,主要进行if (UseBiasedLocking) 判断，如果使用偏向锁，则进入快速重试。否则则进行到重量级锁的slow_enter。
```shell script
//%note monitor_1
IRT_ENTRY_NO_ASYNC(void, InterpreterRuntime::monitorenter(JavaThread* thread, BasicObjectLock* elem))
#ifdef ASSERT
  thread->last_frame().interpreter_frame_verify_monitor(elem);
#endif
  if (PrintBiasedLockingStatistics) {
    Atomic::inc(BiasedLocking::slow_path_entry_count_addr());
  }
  Handle h_obj(thread, elem->obj());
  assert(Universe::heap()->is_in_reserved_or_null(h_obj()),
         "must be NULL or an object");
  if (UseBiasedLocking) {
    // Retry fast entry if bias is revoked to avoid unnecessary inflation
    ObjectSynchronizer::fast_enter(h_obj, elem->lock(), true, CHECK);
  } else {
    ObjectSynchronizer::slow_enter(h_obj, elem->lock(), CHECK);
  }
  assert(Universe::heap()->is_in_reserved_or_null(elem->obj()),
         "must be NULL or an object");
#ifdef ASSERT
  thread->last_frame().interpreter_frame_verify_monitor(elem);
#endif
IRT_END
```

2.ObjectSynchronizer::slow_enter最后会走到ObjectMonitor::enter（src/share/vm/runtime/objectMonitor.cpp）获取锁

此处省略锁的自旋优化等操作。以上代码的具体流程概括如下：

1）通过CAS尝试把monitor的owner字段设置为当前线程。
2）如果设置之前的owner指向当前线程，说明当前线程再次进入monitor，即重入锁，执行 recursions ++ ，记录重入的次数。
3）如果当前线程是第一次进入该monitor，设置recursions为1，_owner为当前线程，该线程成功获得锁并返回。
4）如果获取锁失败，则等待锁的释放。使用EnterI()方法

```shell script
void ATTR ObjectMonitor::enter(TRAPS) {
  // The following code is ordered to check the most common cases first
  // and to reduce RTS->RTO cache line upgrades on SPARC and IA32 processors.
  Thread * const Self = THREAD ;
  void * cur ;

  cur = Atomic::cmpxchg_ptr (Self, &_owner, NULL) ;
  if (cur == NULL) {
     // Either ASSERT _recursions == 0 or explicitly set _recursions = 0.
     assert (_recursions == 0   , "invariant") ;
     assert (_owner      == Self, "invariant") ;
     // CONSIDER: set or assert OwnerIsThread == 1
     return ;
  }

  if (cur == Self) {
     // TODO-FIXME: check for integer overflow!  BUGID 6557169.
     _recursions ++ ;
     return ;
  }

  if (Self->is_lock_owned ((address)cur)) {
    assert (_recursions == 0, "internal state error");
    _recursions = 1 ;
    // Commute owner from a thread-specific on-stack BasicLockObject address to
    // a full-fledged "Thread *".
    _owner = Self ;
    OwnerIsThread = 1 ;
    return ;
  }

  // We've encountered genuine contention.
  assert (Self->_Stalled == 0, "invariant") ;
  Self->_Stalled = intptr_t(this) ;

  // Try one round of spinning *before* enqueueing Self
  // and before going through the awkward and expensive state
  // transitions.  The following spin is strictly optional ...
  // Note that if we acquire the monitor from an initial spin
  // we forgo posting JVMTI events and firing DTRACE probes.
  if (Knob_SpinEarly && TrySpin (Self) > 0) {
     assert (_owner == Self      , "invariant") ;
     assert (_recursions == 0    , "invariant") ;
     assert (((oop)(object()))->mark() == markOopDesc::encode(this), "invariant") ;
     Self->_Stalled = 0 ;
     return ;
  }

  assert (_owner != Self          , "invariant") ;
  assert (_succ  != Self          , "invariant") ;
  assert (Self->is_Java_thread()  , "invariant") ;
  JavaThread * jt = (JavaThread *) Self ;
  assert (!SafepointSynchronize::is_at_safepoint(), "invariant") ;
  assert (jt->thread_state() != _thread_blocked   , "invariant") ;
  assert (this->object() != NULL  , "invariant") ;
  assert (_count >= 0, "invariant") ;

  // Prevent deflation at STW-time.  See deflate_idle_monitors() and is_busy().
  // Ensure the object-monitor relationship remains stable while there's contention.
  Atomic::inc_ptr(&_count);

  EventJavaMonitorEnter event;

  { // Change java thread status to indicate blocked on monitor enter.
    JavaThreadBlockedOnMonitorEnterState jtbmes(jt, this);

    DTRACE_MONITOR_PROBE(contended__enter, this, object(), jt);
    if (JvmtiExport::should_post_monitor_contended_enter()) {
      JvmtiExport::post_monitor_contended_enter(jt, this);
    }

    OSThreadContendState osts(Self->osthread());
    ThreadBlockInVM tbivm(jt);

    Self->set_current_pending_monitor(this);

    // TODO-FIXME: change the following for(;;) loop to straight-line code.
    for (;;) {
      jt->set_suspend_equivalent();
      // cleared by handle_special_suspend_equivalent_condition()
      // or java_suspend_self()

      EnterI (THREAD) ;

      if (!ExitSuspendEquivalent(jt)) break ;

      //
      // We have acquired the contended monitor, but while we were
      // waiting another thread suspended us. We don't want to enter
      // the monitor while suspended because that would surprise the
      // thread that suspended us.
      //
          _recursions = 0 ;
      _succ = NULL ;
      exit (false, Self) ;

      jt->java_suspend_self();
    }
    Self->set_current_pending_monitor(NULL);
  }

  Atomic::dec_ptr(&_count);
  assert (_count >= 0, "invariant") ;
  Self->_Stalled = 0 ;

  // Must either set _recursions = 0 or ASSERT _recursions == 0.
  assert (_recursions == 0     , "invariant") ;
  assert (_owner == Self       , "invariant") ;
  assert (_succ  != Self       , "invariant") ;
  assert (((oop)(object()))->mark() == markOopDesc::encode(this), "invariant") ;

  // The thread -- now the owner -- is back in vm mode.
  // Report the glorious news via TI,DTrace and jvmstat.
  // The probe effect is non-trivial.  All the reportage occurs
  // while we hold the monitor, increasing the length of the critical
  // section.  Amdahl's parallel speedup law comes vividly into play.
  //
  // Another option might be to aggregate the events (thread local or
  // per-monitor aggregation) and defer reporting until a more opportune
  // time -- such as next time some thread encounters contention but has
  // yet to acquire the lock.  While spinning that thread could
  // spinning we could increment JVMStat counters, etc.

  DTRACE_MONITOR_PROBE(contended__entered, this, object(), jt);
  if (JvmtiExport::should_post_monitor_contended_entered()) {
    JvmtiExport::post_monitor_contended_entered(jt, this);
  }

  if (event.should_commit()) {
    event.set_klass(((oop)this->object())->klass());
    event.set_previousOwner((TYPE_JAVALANGTHREAD)_previous_owner_tid);
    event.set_address((TYPE_ADDRESS)(uintptr_t)(this->object_addr()));
    event.commit();
  }

  if (ObjectMonitor::_sync_ContendedLockAttempts != NULL) {
     ObjectMonitor::_sync_ContendedLockAttempts->inc() ;
  }
}
```

### 锁等待
如果上一步的竞争失败等待调用的是ObjectMonitor对象的EnterI方法（src/share/vm/runtime/objectMonitor.cpp），然后执行:

1.将当前线程封装为ObjectWaiter对象node，状态设置为ObjectWaiter::TS_CXQ,通过CAS操作把node节点push到_cxq队列中，同一时刻可能有多个线程把自己的node节点push到cxq列表中。。 

2.在_cxq队列中执行排队等待

3.最后从队列头出队获取锁，当该线程被唤醒时，会从挂起的点继续执行，通过 ObjectMonitor::TryLock 尝试获取锁，TryLock方法。 

源码如下所示：
```shell script
void ATTR ObjectMonitor::EnterI (TRAPS) {
    Thread * Self = THREAD ;
    assert (Self->is_Java_thread(), "invariant") ;
    assert (((JavaThread *) Self)->thread_state() == _thread_blocked   , "invariant") ;

    // Try the lock - TATAS
    if (TryLock (Self) > 0) {
        assert (_succ != Self              , "invariant") ;
        assert (_owner == Self             , "invariant") ;
        assert (_Responsible != Self       , "invariant") ;
        return ;
    }

    DeferredInitialize () ;

    // We try one round of spinning *before* enqueueing Self.
    //
    // If the _owner is ready but OFFPROC we could use a YieldTo()
    // operation to donate the remainder of this thread's quantum
    // to the owner.  This has subtle but beneficial affinity
    // effects.

    if (TrySpin (Self) > 0) {
        assert (_owner == Self        , "invariant") ;
        assert (_succ != Self         , "invariant") ;
        assert (_Responsible != Self  , "invariant") ;
        return ;
    }

    // The Spin failed -- Enqueue and park the thread ...
    assert (_succ  != Self            , "invariant") ;
    assert (_owner != Self            , "invariant") ;
    assert (_Responsible != Self      , "invariant") ;

    // Enqueue "Self" on ObjectMonitor's _cxq.
    //
    // Node acts as a proxy for Self.
    // As an aside, if were to ever rewrite the synchronization code mostly
    // in Java, WaitNodes, ObjectMonitors, and Events would become 1st-class
    // Java objects.  This would avoid awkward lifecycle and liveness issues,
    // as well as eliminate a subset of ABA issues.
    // TODO: eliminate ObjectWaiter and enqueue either Threads or Events.
    //

    ObjectWaiter node(Self) ;
    Self->_ParkEvent->reset() ;
    node._prev   = (ObjectWaiter *) 0xBAD ;
    node.TState  = ObjectWaiter::TS_CXQ ;

    // Push "Self" onto the front of the _cxq.
    // Once on cxq/EntryList, Self stays on-queue until it acquires the lock.
    // Note that spinning tends to reduce the rate at which threads
    // enqueue and dequeue on EntryList|cxq.
    ObjectWaiter * nxt ;
    for (;;) {
        node._next = nxt = _cxq ;
        if (Atomic::cmpxchg_ptr (&node, &_cxq, nxt) == nxt) break ;

        // Interference - the CAS failed because _cxq changed.  Just retry.
        // As an optional optimization we retry the lock.
        if (TryLock (Self) > 0) {
            assert (_succ != Self         , "invariant") ;
            assert (_owner == Self        , "invariant") ;
            assert (_Responsible != Self  , "invariant") ;
            return ;
        }
    }

    // Check for cxq|EntryList edge transition to non-null.  This indicates
    // the onset of contention.  While contention persists exiting threads
    // will use a ST:MEMBAR:LD 1-1 exit protocol.  When contention abates exit
    // operations revert to the faster 1-0 mode.  This enter operation may interleave
    // (race) a concurrent 1-0 exit operation, resulting in stranding, so we
    // arrange for one of the contending thread to use a timed park() operations
    // to detect and recover from the race.  (Stranding is form of progress failure
    // where the monitor is unlocked but all the contending threads remain parked).
    // That is, at least one of the contended threads will periodically poll _owner.
    // One of the contending threads will become the designated "Responsible" thread.
    // The Responsible thread uses a timed park instead of a normal indefinite park
    // operation -- it periodically wakes and checks for and recovers from potential
    // strandings admitted by 1-0 exit operations.   We need at most one Responsible
    // thread per-monitor at any given moment.  Only threads on cxq|EntryList may
    // be responsible for a monitor.
    //
    // Currently, one of the contended threads takes on the added role of "Responsible".
    // A viable alternative would be to use a dedicated "stranding checker" thread
    // that periodically iterated over all the threads (or active monitors) and unparked
    // successors where there was risk of stranding.  This would help eliminate the
    // timer scalability issues we see on some platforms as we'd only have one thread
    // -- the checker -- parked on a timer.

    if ((SyncFlags & 16) == 0 && nxt == NULL && _EntryList == NULL) {
        // Try to assume the role of responsible thread for the monitor.
        // CONSIDER:  ST vs CAS vs { if (Responsible==null) Responsible=Self }
        Atomic::cmpxchg_ptr (Self, &_Responsible, NULL) ;
    }

    // The lock have been released while this thread was occupied queueing
    // itself onto _cxq.  To close the race and avoid "stranding" and
    // progress-liveness failure we must resample-retry _owner before parking.
    // Note the Dekker/Lamport duality: ST cxq; MEMBAR; LD Owner.
    // In this case the ST-MEMBAR is accomplished with CAS().
    //
    // TODO: Defer all thread state transitions until park-time.
    // Since state transitions are heavy and inefficient we'd like
    // to defer the state transitions until absolutely necessary,
    // and in doing so avoid some transitions ...

    TEVENT (Inflated enter - Contention) ;
    int nWakeups = 0 ;
    int RecheckInterval = 1 ;

    for (;;) {

        if (TryLock (Self) > 0) break ;
        assert (_owner != Self, "invariant") ;

        if ((SyncFlags & 2) && _Responsible == NULL) {
           Atomic::cmpxchg_ptr (Self, &_Responsible, NULL) ;
        }

        // park self
        if (_Responsible == Self || (SyncFlags & 1)) {
            TEVENT (Inflated enter - park TIMED) ;
            Self->_ParkEvent->park ((jlong) RecheckInterval) ;
            // Increase the RecheckInterval, but clamp the value.
            RecheckInterval *= 8 ;
            if (RecheckInterval > 1000) RecheckInterval = 1000 ;
        } else {
            TEVENT (Inflated enter - park UNTIMED) ;
            Self->_ParkEvent->park() ;
        }

        if (TryLock(Self) > 0) break ;

        // The lock is still contested.
        // Keep a tally of the # of futile wakeups.
        // Note that the counter is not protected by a lock or updated by atomics.
        // That is by design - we trade "lossy" counters which are exposed to
        // races during updates for a lower probe effect.
        TEVENT (Inflated enter - Futile wakeup) ;
        if (ObjectMonitor::_sync_FutileWakeups != NULL) {
           ObjectMonitor::_sync_FutileWakeups->inc() ;
        }
        ++ nWakeups ;

        // Assuming this is not a spurious wakeup we'll normally find _succ == Self.
        // We can defer clearing _succ until after the spin completes
        // TrySpin() must tolerate being called with _succ == Self.
        // Try yet another round of adaptive spinning.
        if ((Knob_SpinAfterFutile & 1) && TrySpin (Self) > 0) break ;

        // We can find that we were unpark()ed and redesignated _succ while
        // we were spinning.  That's harmless.  If we iterate and call park(),
        // park() will consume the event and return immediately and we'll
        // just spin again.  This pattern can repeat, leaving _succ to simply
        // spin on a CPU.  Enable Knob_ResetEvent to clear pending unparks().
        // Alternately, we can sample fired() here, and if set, forgo spinning
        // in the next iteration.

        if ((Knob_ResetEvent & 1) && Self->_ParkEvent->fired()) {
           Self->_ParkEvent->reset() ;
           OrderAccess::fence() ;
        }
        if (_succ == Self) _succ = NULL ;

        // Invariant: after clearing _succ a thread *must* retry _owner before parking.
        OrderAccess::fence() ;
    }

    // Egress :
    // Self has acquired the lock -- Unlink Self from the cxq or EntryList.
    // Normally we'll find Self on the EntryList .
    // From the perspective of the lock owner (this thread), the
    // EntryList is stable and cxq is prepend-only.
    // The head of cxq is volatile but the interior is stable.
    // In addition, Self.TState is stable.

    assert (_owner == Self      , "invariant") ;
    assert (object() != NULL    , "invariant") ;
    // I'd like to write:
    //   guarantee (((oop)(object()))->mark() == markOopDesc::encode(this), "invariant") ;
    // but as we're at a safepoint that's not safe.

    UnlinkAfterAcquire (Self, &node) ;
    if (_succ == Self) _succ = NULL ;

    assert (_succ != Self, "invariant") ;
    if (_Responsible == Self) {
        _Responsible = NULL ;
        OrderAccess::fence(); // Dekker pivot-point

        // We may leave threads on cxq|EntryList without a designated
        // "Responsible" thread.  This is benign.  When this thread subsequently
        // exits the monitor it can "see" such preexisting "old" threads --
        // threads that arrived on the cxq|EntryList before the fence, above --
        // by LDing cxq|EntryList.  Newly arrived threads -- that is, threads
        // that arrive on cxq after the ST:MEMBAR, above -- will set Responsible
        // non-null and elect a new "Responsible" timer thread.
        //
        // This thread executes:
        //    ST Responsible=null; MEMBAR    (in enter epilog - here)
        //    LD cxq|EntryList               (in subsequent exit)
        //
        // Entering threads in the slow/contended path execute:
        //    ST cxq=nonnull; MEMBAR; LD Responsible (in enter prolog)
        //    The (ST cxq; MEMBAR) is accomplished with CAS().
        //
        // The MEMBAR, above, prevents the LD of cxq|EntryList in the subsequent
        // exit operation from floating above the ST Responsible=null.
    }

    // We've acquired ownership with CAS().
    // CAS is serializing -- it has MEMBAR/FENCE-equivalent semantics.
    // But since the CAS() this thread may have also stored into _succ,
    // EntryList, cxq or Responsible.  These meta-data updates must be
    // visible __before this thread subsequently drops the lock.
    // Consider what could occur if we didn't enforce this constraint --
    // STs to monitor meta-data and user-data could reorder with (become
    // visible after) the ST in exit that drops ownership of the lock.
    // Some other thread could then acquire the lock, but observe inconsistent
    // or old monitor meta-data and heap data.  That violates the JMM.
    // To that end, the 1-0 exit() operation must have at least STST|LDST
    // "release" barrier semantics.  Specifically, there must be at least a
    // STST|LDST barrier in exit() before the ST of null into _owner that drops
    // the lock.   The barrier ensures that changes to monitor meta-data and data
    // protected by the lock will be visible before we release the lock, and
    // therefore before some other thread (CPU) has a chance to acquire the lock.
    // See also: http://gee.cs.oswego.edu/dl/jmm/cookbook.html.
    //
    // Critically, any prior STs to _succ or EntryList must be visible before
    // the ST of null into _owner in the *subsequent* (following) corresponding
    // monitorexit.  Recall too, that in 1-0 mode monitorexit does not necessarily
    // execute a serializing instruction.

    if (SyncFlags & 8) {
       OrderAccess::fence() ;
    }
    return ;
}

```


### 锁释放
当某个持有锁的线程执行完同步代码块时，会进行锁的释放，给其它线程机会执行同步代码，在HotSpot中，通过退出monitor的方式实现锁的释放，并通知被阻塞的线程，具体实现位于
ObjectMonitor::exit方法中。（src/share/vm/runtime/objectMonitor.cpp）

1.退出同步代码块时会让recursions减1，当recursions的值减为0时，说明线程释放了锁。

2.根据不同的策略（由QMode指定），从cxq或EntryList中获取头节点，通过ObjectMonitor::ExitEpilog 方法唤醒该节点封装的线程，唤醒操作最终由unpark完成：

3.被唤醒的线程，会回到 void ATTR ObjectMonitor::EnterI (TRAPS) 的第600行，继续执行monitor的竞争。

```shell script
void ATTR ObjectMonitor::exit(bool not_suspended, TRAPS) {
   Thread * Self = THREAD ;
   if (THREAD != _owner) {
     if (THREAD->is_lock_owned((address) _owner)) {
       // Transmute _owner from a BasicLock pointer to a Thread address.
       // We don't need to hold _mutex for this transition.
       // Non-null to Non-null is safe as long as all readers can
       // tolerate either flavor.
       assert (_recursions == 0, "invariant") ;
       _owner = THREAD ;
       _recursions = 0 ;
       OwnerIsThread = 1 ;
     } else {
       // NOTE: we need to handle unbalanced monitor enter/exit
       // in native code by throwing an exception.
       // TODO: Throw an IllegalMonitorStateException ?
       TEVENT (Exit - Throw IMSX) ;
       assert(false, "Non-balanced monitor enter/exit!");
       if (false) {
          THROW(vmSymbols::java_lang_IllegalMonitorStateException());
       }
       return;
     }
   }

   if (_recursions != 0) {
     _recursions--;        // this is simple recursive enter
     TEVENT (Inflated exit - recursive) ;
     return ;
   }

   // Invariant: after setting Responsible=null an thread must execute
   // a MEMBAR or other serializing instruction before fetching EntryList|cxq.
   if ((SyncFlags & 4) == 0) {
      _Responsible = NULL ;
   }

#if INCLUDE_TRACE
   // get the owner's thread id for the MonitorEnter event
   // if it is enabled and the thread isn't suspended
   if (not_suspended && Tracing::is_event_enabled(TraceJavaMonitorEnterEvent)) {
     _previous_owner_tid = SharedRuntime::get_java_tid(Self);
   }
#endif

   for (;;) {
      assert (THREAD == _owner, "invariant") ;


      if (Knob_ExitPolicy == 0) {
         // release semantics: prior loads and stores from within the critical section
         // must not float (reorder) past the following store that drops the lock.
         // On SPARC that requires MEMBAR #loadstore|#storestore.
         // But of course in TSO #loadstore|#storestore is not required.
         // I'd like to write one of the following:
         // A.  OrderAccess::release() ; _owner = NULL
         // B.  OrderAccess::loadstore(); OrderAccess::storestore(); _owner = NULL;
         // Unfortunately OrderAccess::release() and OrderAccess::loadstore() both
         // store into a _dummy variable.  That store is not needed, but can result
         // in massive wasteful coherency traffic on classic SMP systems.
         // Instead, I use release_store(), which is implemented as just a simple
         // ST on x64, x86 and SPARC.
         OrderAccess::release_store_ptr (&_owner, NULL) ;   // drop the lock
         OrderAccess::storeload() ;                         // See if we need to wake a successor
         if ((intptr_t(_EntryList)|intptr_t(_cxq)) == 0 || _succ != NULL) {
            TEVENT (Inflated exit - simple egress) ;
            return ;
         }
         TEVENT (Inflated exit - complex egress) ;

         // Normally the exiting thread is responsible for ensuring succession,
         // but if other successors are ready or other entering threads are spinning
         // then this thread can simply store NULL into _owner and exit without
         // waking a successor.  The existence of spinners or ready successors
         // guarantees proper succession (liveness).  Responsibility passes to the
         // ready or running successors.  The exiting thread delegates the duty.
         // More precisely, if a successor already exists this thread is absolved
         // of the responsibility of waking (unparking) one.
         //
         // The _succ variable is critical to reducing futile wakeup frequency.
         // _succ identifies the "heir presumptive" thread that has been made
         // ready (unparked) but that has not yet run.  We need only one such
         // successor thread to guarantee progress.
         // See http://www.usenix.org/events/jvm01/full_papers/dice/dice.pdf
         // section 3.3 "Futile Wakeup Throttling" for details.
         //
         // Note that spinners in Enter() also set _succ non-null.
         // In the current implementation spinners opportunistically set
         // _succ so that exiting threads might avoid waking a successor.
         // Another less appealing alternative would be for the exiting thread
         // to drop the lock and then spin briefly to see if a spinner managed
         // to acquire the lock.  If so, the exiting thread could exit
         // immediately without waking a successor, otherwise the exiting
         // thread would need to dequeue and wake a successor.
         // (Note that we'd need to make the post-drop spin short, but no
         // shorter than the worst-case round-trip cache-line migration time.
         // The dropped lock needs to become visible to the spinner, and then
         // the acquisition of the lock by the spinner must become visible to
         // the exiting thread).
         //

         // It appears that an heir-presumptive (successor) must be made ready.
         // Only the current lock owner can manipulate the EntryList or
         // drain _cxq, so we need to reacquire the lock.  If we fail
         // to reacquire the lock the responsibility for ensuring succession
         // falls to the new owner.
         //
         if (Atomic::cmpxchg_ptr (THREAD, &_owner, NULL) != NULL) {
            return ;
         }
         TEVENT (Exit - Reacquired) ;
      } else {
         if ((intptr_t(_EntryList)|intptr_t(_cxq)) == 0 || _succ != NULL) {
            OrderAccess::release_store_ptr (&_owner, NULL) ;   // drop the lock
            OrderAccess::storeload() ;
            // Ratify the previously observed values.
            if (_cxq == NULL || _succ != NULL) {
                TEVENT (Inflated exit - simple egress) ;
                return ;
            }

            // inopportune interleaving -- the exiting thread (this thread)
            // in the fast-exit path raced an entering thread in the slow-enter
            // path.
            // We have two choices:
            // A.  Try to reacquire the lock.
            //     If the CAS() fails return immediately, otherwise
            //     we either restart/rerun the exit operation, or simply
            //     fall-through into the code below which wakes a successor.
            // B.  If the elements forming the EntryList|cxq are TSM
            //     we could simply unpark() the lead thread and return
            //     without having set _succ.
            if (Atomic::cmpxchg_ptr (THREAD, &_owner, NULL) != NULL) {
               TEVENT (Inflated exit - reacquired succeeded) ;
               return ;
            }
            TEVENT (Inflated exit - reacquired failed) ;
         } else {
            TEVENT (Inflated exit - complex egress) ;
         }
      }

      guarantee (_owner == THREAD, "invariant") ;

      ObjectWaiter * w = NULL ;
      int QMode = Knob_QMode ;

      if (QMode == 2 && _cxq != NULL) {
          // QMode == 2 : cxq has precedence over EntryList.
          // Try to directly wake a successor from the cxq.
          // If successful, the successor will need to unlink itself from cxq.
          w = _cxq ;
          assert (w != NULL, "invariant") ;
          assert (w->TState == ObjectWaiter::TS_CXQ, "Invariant") ;
          ExitEpilog (Self, w) ;
          return ;
      }

      if (QMode == 3 && _cxq != NULL) {
          // Aggressively drain cxq into EntryList at the first opportunity.
          // This policy ensure that recently-run threads live at the head of EntryList.
          // Drain _cxq into EntryList - bulk transfer.
          // First, detach _cxq.
          // The following loop is tantamount to: w = swap (&cxq, NULL)
          w = _cxq ;
          for (;;) {
             assert (w != NULL, "Invariant") ;
             ObjectWaiter * u = (ObjectWaiter *) Atomic::cmpxchg_ptr (NULL, &_cxq, w) ;
             if (u == w) break ;
             w = u ;
          }
          assert (w != NULL              , "invariant") ;

          ObjectWaiter * q = NULL ;
          ObjectWaiter * p ;
          for (p = w ; p != NULL ; p = p->_next) {
              guarantee (p->TState == ObjectWaiter::TS_CXQ, "Invariant") ;
              p->TState = ObjectWaiter::TS_ENTER ;
              p->_prev = q ;
              q = p ;
          }

          // Append the RATs to the EntryList
          // TODO: organize EntryList as a CDLL so we can locate the tail in constant-time.
          ObjectWaiter * Tail ;
          for (Tail = _EntryList ; Tail != NULL && Tail->_next != NULL ; Tail = Tail->_next) ;
          if (Tail == NULL) {
              _EntryList = w ;
          } else {
              Tail->_next = w ;
              w->_prev = Tail ;
          }

          // Fall thru into code that tries to wake a successor from EntryList
      }

      if (QMode == 4 && _cxq != NULL) {
          // Aggressively drain cxq into EntryList at the first opportunity.
          // This policy ensure that recently-run threads live at the head of EntryList.

          // Drain _cxq into EntryList - bulk transfer.
          // First, detach _cxq.
          // The following loop is tantamount to: w = swap (&cxq, NULL)
          w = _cxq ;
          for (;;) {
             assert (w != NULL, "Invariant") ;
             ObjectWaiter * u = (ObjectWaiter *) Atomic::cmpxchg_ptr (NULL, &_cxq, w) ;
             if (u == w) break ;
             w = u ;
          }
          assert (w != NULL              , "invariant") ;

          ObjectWaiter * q = NULL ;
          ObjectWaiter * p ;
          for (p = w ; p != NULL ; p = p->_next) {
              guarantee (p->TState == ObjectWaiter::TS_CXQ, "Invariant") ;
              p->TState = ObjectWaiter::TS_ENTER ;
              p->_prev = q ;
              q = p ;
          }

          // Prepend the RATs to the EntryList
          if (_EntryList != NULL) {
              q->_next = _EntryList ;
              _EntryList->_prev = q ;
          }
          _EntryList = w ;

          // Fall thru into code that tries to wake a successor from EntryList
      }

      w = _EntryList  ;
      if (w != NULL) {
          // I'd like to write: guarantee (w->_thread != Self).
          // But in practice an exiting thread may find itself on the EntryList.
          // Lets say thread T1 calls O.wait().  Wait() enqueues T1 on O's waitset and
          // then calls exit().  Exit release the lock by setting O._owner to NULL.
          // Lets say T1 then stalls.  T2 acquires O and calls O.notify().  The
          // notify() operation moves T1 from O's waitset to O's EntryList. T2 then
          // release the lock "O".  T2 resumes immediately after the ST of null into
          // _owner, above.  T2 notices that the EntryList is populated, so it
          // reacquires the lock and then finds itself on the EntryList.
          // Given all that, we have to tolerate the circumstance where "w" is
          // associated with Self.
          assert (w->TState == ObjectWaiter::TS_ENTER, "invariant") ;
          ExitEpilog (Self, w) ;
          return ;
      }

      // If we find that both _cxq and EntryList are null then just
      // re-run the exit protocol from the top.
      w = _cxq ;
      if (w == NULL) continue ;

      // Drain _cxq into EntryList - bulk transfer.
      // First, detach _cxq.
      // The following loop is tantamount to: w = swap (&cxq, NULL)
      for (;;) {
          assert (w != NULL, "Invariant") ;
          ObjectWaiter * u = (ObjectWaiter *) Atomic::cmpxchg_ptr (NULL, &_cxq, w) ;
          if (u == w) break ;
          w = u ;
      }
      TEVENT (Inflated exit - drain cxq into EntryList) ;

      assert (w != NULL              , "invariant") ;
      assert (_EntryList  == NULL    , "invariant") ;

      // Convert the LIFO SLL anchored by _cxq into a DLL.
      // The list reorganization step operates in O(LENGTH(w)) time.
      // It's critical that this step operate quickly as
      // "Self" still holds the outer-lock, restricting parallelism
      // and effectively lengthening the critical section.
      // Invariant: s chases t chases u.
      // TODO-FIXME: consider changing EntryList from a DLL to a CDLL so
      // we have faster access to the tail.

      if (QMode == 1) {
         // QMode == 1 : drain cxq to EntryList, reversing order
         // We also reverse the order of the list.
         ObjectWaiter * s = NULL ;
         ObjectWaiter * t = w ;
         ObjectWaiter * u = NULL ;
         while (t != NULL) {
             guarantee (t->TState == ObjectWaiter::TS_CXQ, "invariant") ;
             t->TState = ObjectWaiter::TS_ENTER ;
             u = t->_next ;
             t->_prev = u ;
             t->_next = s ;
             s = t;
             t = u ;
         }
         _EntryList  = s ;
         assert (s != NULL, "invariant") ;
      } else {
         // QMode == 0 or QMode == 2
         _EntryList = w ;
         ObjectWaiter * q = NULL ;
         ObjectWaiter * p ;
         for (p = w ; p != NULL ; p = p->_next) {
             guarantee (p->TState == ObjectWaiter::TS_CXQ, "Invariant") ;
             p->TState = ObjectWaiter::TS_ENTER ;
             p->_prev = q ;
             q = p ;
         }
      }

      // In 1-0 mode we need: ST EntryList; MEMBAR #storestore; ST _owner = NULL
      // The MEMBAR is satisfied by the release_store() operation in ExitEpilog().

      // See if we can abdicate to a spinner instead of waking a thread.
      // A primary goal of the implementation is to reduce the
      // context-switch rate.
      if (_succ != NULL) continue;

      w = _EntryList  ;
      if (w != NULL) {
          guarantee (w->TState == ObjectWaiter::TS_ENTER, "invariant") ;
          ExitEpilog (Self, w) ;
          return ;
      }
   }
}
```



参考文档： 
https://blog.csdn.net/zicair/article/details/107657237
https://cgiirw.github.io/2018/10/14/Blocked02/
https://cgiirw.github.io/2018/10/17/Blocked03/
https://mp.weixin.qq.com/s/A1bZJlhgUnhHcxURhv_--w
https://www.linuxidc.com/Linux/2018-02/150798.htm
https://juejin.cn/post/6995156191286394888
https://www.infoq.cn/article/bpwrqge-tuubmmz5rqtc