
## ParNew垃圾回收器

一款多线程的收集器，采用复制算法，主要工作在 Young 区，可以通过 -XX:ParallelGCThreads 参数来控制收集的线程数，整个过程都是 STW 的，常与 CMS 组合使用。

G1的Young GC和CMS的Young GC，其标记-复制全过程STW。

## CMS垃圾回收器
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps 查看GC日志可得CMS的细节流程，一共有7个。加上中间的5个中间状态，Log会打一共12个状态。    
[cms-initial-mark] 初始化标记阶段，标记老年代的所有GC Roots；标记年轻代中所有存活对象引用的对象。 触发条件，老年代达到92%。 会STW。  

[cms-current-mark] 并发标记阶段，遍历整个老年代并标记存活对象。GC线程和应用线程同时运行，所以并不能保证标记完全正确。  

[cms-current-preclean]  并发标记阶段，如果有对象在上个阶段引用有变化，则标记为脏页（daity page）

[cms-current-abortable-preclean]  并发标记阶段，尝试去承担STW的CMS Remark阶段足够多的动作。

[cms-remark]  重标记阶段，第二个且是最后一个STW。完成标记整个老年代的所有存活对象。  

[cms-current-sweep]  并发清理，和应用线程同时进行，移除那些不用的对象，且回收空间为将来使用。

[cms-current-reset]  并发重置，重新设置CMS算法内部数据结构，为下一个CMS生命周期做准备。  



CMS的问题：  
1) 并发标记、并发回收两个阶段不会STW，但GC线程会占用CPU导致吞吐量下降，默认回收线程（CPU核数+3）/4，如果是一台物理机跑，那就同时起了十几个线程。  
   实际遇到的一个问题就有:[cms-current-mark] 80.642/248.547 sec,普通这个阶段应该要在1s左右，默认应该是4 GC线程。

2) 无法清理浮动垃圾。并发标记和并发清理阶段，总会有垃圾在标记结束后出现，此时CMS无法清理，只能留到下次清理。

3) 并发失败。因为垃圾回收阶段用户线程并发，需要留出足够内存给用户线程使用。因为并不会在100%接近的时候GC，而是默认92%就需要GC了。如果92%的时候，无法满足用户申请对象的需求，则会启用备用方法，启动Serial Old来进行老年代回收，这样一来停顿时间就很长了。  
   (current mode failure)： 7914399k -> 679210k (8047872k),322.8811699 sec 因为并发失败，退化成单线程回收，耗时相当恐怖，而这部分Serial Old直接进行STW，长时间无反应。需要dump是什么导致的内存占用，什么在晋升导致的回收。

4) 内存碎片问题。标记清除算法的内存碎片问题。默认开启整理。


CMS是一种基于标记-清理算法的垃圾回收器，这就意味着会产生空间碎片。解决办法一般是配置参数约束CMS在一定次数后进行整理。注意可以强制remark之前开始一次minor gc，减少remark的暂停时间。
```
-XX:+UseCMSCompactAtFullCollection 
-XX:CMSFullGCsBeforeCompaction=5
-XX:+UseCMSInitiatingOccupancyOnly
-XX:CMSInitiatingOccupancyFraction=80
-XX:+CMSScavengeBeforeRemark
```

参考文档：  
https://bugs.openjdk.java.net/browse/JDK-8027132
https://www.cnblogs.com/chiangchou/p/jvm-2.html#_label2_5


## G1垃圾回收器
G1（Garbage First）回收器采用面向局部收集的设计思路和基于Region的内存布局形式，是一款主要面向服务端应用的垃圾回收器。G1设计初衷就是替换 CMS，成为一种全功能收集器。G1 在JDK9 之后成为服务端模式下的默认垃圾回收器，取代了 Parallel Scavenge 加 Parallel Old 的默认组合，而 CMS 被声明为不推荐使用的垃圾回收器。G1从整体来看是基于 标记-整理 算法实现的回收器，但从局部（两个Region之间）上看又是基于 标记-复制 算法实现的

```
2020-09-27T23:16:05.118+0800: 0.217: [GC pause (G1 Evacuation Pause) (young), 0.0090108 secs]
   [Eden: 100.0M(100.0M)->0.0B(84.0M) Survivors: 0.0B->16.0M Heap: 100.0M(200.0M)->95.1M(200.0M)]
2020-09-27T23:16:05.130+0800: 0.228: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 0.0031955 secs]
   [Eden: 84.0M(84.0M)->0.0B(92.0M) Survivors: 16.0M->8192.0K Heap: 179.1M(200.0M)->124.0M(200.0M)]
2020-09-27T23:16:05.136+0800: 0.235: [GC pause (G1 Evacuation Pause) (young) (initial-mark) (to-space exhausted), 0.0010715 secs]
   [Eden: 76.0M(92.0M)->0.0B(100.0M) Survivors: 8192.0K->0.0B Heap: 200.0M(200.0M)->128.0M(200.0M)]
 [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-09-27T23:16:05.137+0800: 0.236: [GC concurrent-root-region-scan-start]
2020-09-27T23:16:05.137+0800: 0.236: [GC concurrent-root-region-scan-end, 0.0000095 secs]
2020-09-27T23:16:05.137+0800: 0.236: [GC concurrent-mark-start]
2020-09-27T23:16:05.138+0800: 0.237: [GC concurrent-mark-end, 0.0012656 secs]
2020-09-27T23:16:05.138+0800: 0.237: [GC remark 2020-09-27T23:16:05.139+0800: 0.237: [Finalize Marking, 0.0001289 secs] 2020-09-27T23:16:05.139+0800: 0.238: [GC ref-proc, 0.0005058 secs] 2020-09-27T23:16:05.139+0800: 0.238: [Unloading, 0.0004047 secs], 0.0013203 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-09-27T23:16:05.140+0800: 0.239: [GC cleanup 132M->132M(200M), 0.0005312 secs]
 [Times: user=0.00 sys=0.00, real=0.00 secs]
Heap
 garbage-first heap   total 204800K, used 135168K [0x00000000f3800000, 0x00000000f3c00190, 0x0000000100000000)
  region size 4096K, 2 young (8192K), 0 survivors (0K)
 Metaspace       used 3048K, capacity 4556K, committed 4864K, reserved 1056768K
  class space    used 322K, capacity 392K, committed 512K, reserved 1048576K
```

查看GC日志可得G1的细节流程，一共有5个状态。
G1 回收器的运作过程大致可分为四个步骤：

1）初始标记（会STW）：仅仅只是标记一下 GC Roots 能直接关联到的对象，并且修改TAMS指针的值，让下一阶段用户线程并发运行时，能正确地在可用的Region中分配新对象。这个阶段需要停顿线程，但耗时很短，而且是借用进行Minor GC的时候同步完成的，所以G1收集器在这个阶段实际并没有额外的停顿。
2）并发标记：从 GC Roots 开始对堆中对象进行可达性分析，递归扫描整个堆里的对象图，找出要回收的对象，这阶段耗时较长，但可与用户程序并发执行。当对象图扫描完成以后，还要重新处理在并发时有引用变动的对象。
3）最终标记（会STW）：对用户线程做短暂的暂停，处理并发阶段结束后仍有引用变动的对象。
4）清理阶段（会STW）：更新Region的统计数据，对各个Region的回收价值和成本进行排序，根据用户所期望的停顿时间来制定回收计划，可以自由选择任意多个Region构成回收集，然后把决定回收的那一部分Region的存活对象复制到空的Region中，再清理掉整个旧Region的全部空间。这里的操作涉及存活对象的移动，必须暂停用户线程，由多条回收器线程并行完成的。


G1的特点：
1) 可预期的回收停顿时间.G1 可以指定垃圾回收的停顿时间，通过 -XX: MaxGCPauseMillis 参数指定，默认为 200 毫秒。这个值不宜设置过低，否则会导致每次回收只占堆内存很小的一部分，回收器的回收速度逐渐赶不上对象分配速度，导致垃圾慢慢堆积，最终占满堆内存导致 Full GC 反而降低性能。

2) G1内存布局.G1不再是固定大小以及固定数量的分代区域划分，而是把堆划分为多个大小相等的Region，每个Region的大小默认情况下是堆内存大小除以2048，因为JVM最多可以有2048个Region，而且每个Region的大小必须是2的N次冥。每个Region的大小也可以通过参数 -XX:G1HeapRegionSize 设定，取值范围为1MB～32MB，且应为2的N次幂。

3) 大对象Region.Region中还有一类特殊的 Humongous 区域，专门用来存储大对象，而不是直接进入老年代的Region。G1认为一个对象只要大小超过了一个Region容量的一半就判定为大对象。而对于那些超过了整个Region容量的超级大对象，将会被存放在N个连续的 Humongous Region 之中，G1的大多数行为都把 Humongous Region 作为老年代的一部分来看待。

4) G1新生代回收.根据G1的内存布局举个例子，例如：设置堆内存 4G，就是 4096M，除以2048个Region，每个Region就是2M；新生代期初占5%，就是约100个Region，此时eden区占80个Region，两个survivor区各占10个Region；不过随着对象的在新生代分配，属于新生代的Region会不断增加，eden和survivor对应的Region也会不断增加。直到新生代占用60%，也就是约1200个Region，就会触发新生代的GC，这个时候就会采用复制算法将eden对应Region存活的对象复制到 from survivor 对应的Region。只不过这里会根据用户期望的停顿时间来选取部分最有回收价值的Region进行回收。

5) G1混合回收.G1有一个参数，-XX:InitiatingHeapOccupancyPercent，它的默认值是45%，就是如果老年代占堆内存45%的Region的时候，此时就会触发一次年轻代+老年代的混合回收。
   混合回收阶段，因为我们设定了最大停顿时间，所以 G1 会从新生代、老年代、大对象里挑选一些 Region，保证指定的时间内回收尽可能多的垃圾。所以 G1 可能一次无法将所有Region回收完，它就会执行多次混合回收，先停止程序，执行一次混合回收回收掉一些Region，接着恢复系统运行，然后再次停止系统运行，再执行一次混合回收回收掉一些Region。可以通过参数 -XX:G1MixedGCCountTarget 设置一次回收的过程中，最后一个阶段最多执行几次混合回收，默认值是8次。通过这种反复回收的方式，避免系统长时间的停顿。

G1的问题：
1) 更大的内存占用。由于Region数量比传统回收器的分代数量明显要多得多，因此G1回收器要比其他的传统垃圾回收器有着更高的内存占用负担。G1至少要耗费大约相当于Java堆容量10%至20%的额外内存来维持回收器工作。

2) 并发回收失败.在并发标记阶段，用户线程还在并发运行，程序继续运行就会持续有新对象产生，也需要预留足够的空间提供给用户线程使用。如果内存回收的速度赶不上内存分配的速度，跟CMS会发生并发失败一样，G1也要被迫暂停程序，导致 Full GC 而产生长时间 Stop The World。

3) 混合回收失败.混合回收阶段，年轻代和老年代都是基于复制算法进行回收，复制的过程中如果没有空闲的Region了，就会触发失败。一旦失败，就会停止程序，然后采用单线程标记、清理和内存碎片整理，然后空闲出来一批Region。这个过程是很慢的，因此要尽量调优避免混合回收失败的发生。

```

```

参考文档：  
https://tech.meituan.com/2016/09/23/g1.html

## ZGC
ZGC（The Z Garbage Collector）是JDK 11中推出的一款低延迟垃圾回收器，它的设计目标包括：

停顿时间不超过10ms；
停顿时间不会随着堆的大小，或者活跃对象的大小而增加；
支持8MB~4TB级别的堆（未来支持16TB）。

ZGC只有三个STW阶段：初始标记，再标记，初始转移。其中，初始标记和初始转移分别都只需要扫描所有GC Roots，其处理时间和GC Roots的数量成正比，一般情况耗时非常短；再标记阶段STW时间很短，最多1ms，超过1ms则再次进入并发标记阶段。即，**ZGC几乎所有暂停都只依赖于GC Roots集合大小**，停顿时间不会随着堆的大小或者活跃对象的大小而增加。与ZGC对比，G1的转移阶段完全STW的，且停顿时间随存活对象的大小增加而增加。  


```
-Xms10G -Xmx10G 
-XX:ReservedCodeCacheSize=256m -XX:InitialCodeCacheSize=256m 
-XX:+UnlockExperimentalVMOptions -XX:+UseZGC 
-XX:ConcGCThreads=2 -XX:ParallelGCThreads=6 
-XX:ZCollectionInterval=120 -XX:ZAllocationSpikeTolerance=5 
-XX:+UnlockDiagnosticVMOptions -XX:-ZProactive 
-Xlog:safepoint,classhisto*=trace,age*,gc*=info:file=/opt/logs/logs/gc-%t.log:time,tid,tags:filecount=5,filesize=5
```


参考文档：  
https://wiki.openjdk.java.net/display/zgc/Main
https://tech.meituan.com/2020/08/06/new-zgc-practice-in-meituan.html