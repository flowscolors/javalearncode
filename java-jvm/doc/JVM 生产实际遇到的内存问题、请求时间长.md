
Oracle官网关于JVM的调优指南  
https://docs.oracle.com/javase/10/gctuning/factors-affecting-garbage-collection-performance.htm#JSGCT-GUID-5508674B-F32D-4B02-9002-D0D8C7CDDC75
https://docs.oracle.com/cd/E21764_01/web.1111/e13814/jvm_tuning.htm#PERFM150

## 关于Java程序-Xms配置8G，启动时只占用2G内存
这里需要说明的有两点：
1.JVM声明的-Xms -Xmx这些是会使用的物理内存的，并且GC时候并不会释放这些物理内存，只是标记为空闲，并不是free还给操作系统。基本可以认为会一直占用这些内存，所以会出现top看占用8G，实际jmap -dump只用了2G的情况。  

为什么不把内存归还给操作系统？JVM 还是会归还内存给操作系统的，只是因为这个代价比较大，所以不会轻易进行。而且不同垃圾回收器 的内存分配算法不同，归还内存的代价也不同。

2.虽然JVM是会占用物理内存，但是要注意的最开始启动时候并没有立马占用这些内存。因为进程在申请内存时，并不是直接分配物理内存，而是分配一个虚拟内存。到真正堆这块虚拟空间写入数据时才会通过缺页异常（Page Fault）处理机制分配物理内存，也就是我们看到的进程 Res 指标。  

也就是最开始启动时并没有占用物理内存，但是占了就不会轻易还给你了。

参考文档：
https://segmentfault.com/a/1190000040050819

## 关于Java程序RES占用12G，VIRT占用50G

一般是线程占用，默认一个线程会占用64M虚拟内存

## 一些堆调优技巧
1.应将堆大小设置为不超过最大可用物理RAM量。如果超过此值，操作系统将开始分页，性能会显着下降。VM总是使用比堆大小更多的内存。除了堆大小设置之外，还分配内部VM功能所需的内存，VM外部的本地类库和永久区（仅适用于Sun虚拟机：存储类和方法所需的内存）。

2.使用分代垃圾收集方案时，年轻代大小不应超过Java堆总大小的一半。通常，堆大小的25％到40％就足够了。

3.在生产环境中，将最小堆大小和最大堆大小设置为相同的值，以防止浪费用于不断增长和收缩堆的VM资源。


## 生产中遇到的OOM
Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded

### 1.内存溢出 数据库查询没有limit
* 一次查询返回数据量太多，直接给后端干崩了，arraylist存不下，元素赋值时不允许。虽然每次查出来的arraylist是会被GC，但问题是大部分service并不是查出来返回就结束。
比如没加limit，offset，直接查了300w条数据，并且还对依次300w数据做操作，这个时候内存长时间不释放，full gc无法回收内存，直接OOM.
  
* 一次拉取过多数据做本地缓存。

* 比如大量文本、视频，直接全量加载到内存处理。   

总之都是大对象导致的GC。

### 2.内存泄漏
一般是启动时不会发现，慢慢增加的，比如:
* hashmap有put，没有remove。导致对象越来越大。
* Threadlocal的value未remove导致的内存溢出

客户端内部实现维护了map存对象，而内存泄漏导致最后老年代每次回收都回收不掉 OOM，需要找到对应溢出的地方解决。

参考文档： 
https://mp.weixin.qq.com/s/Imyo_cQ5OWdY9fY0Qz3nzw

### 3.可数循环的JIT优化导致GC时间过长
可数循环会被JIT优化，中间的安全点检查会被跳过，最后才到安全点。导致其他线程就会在这次安全点检查中等待可数循环的任务执行完才进行后面的操作。  
也即部分线程到达安全点、而一些特别慢的线程没有到达，导致先到达的会自旋等待，使用户线程长时间无响应。

### 4.使用默认JVM参数 踩了默认参数的坑
JDK8默认老年代开启自动调整算法。来看一个实际的案例。
在一个limit 2C2G的容器里面，没有配置有关jvm的启动参数，使用默认配置。查看jmap -heap配置
```text
JVM Version is 25.292-b10
Heap Configuration:
    MinHeapFreeRatio     = 0
    MaxHeapFreeRatio     = 100
    MaxHeapSize          = 536870912 (512.0MB)
    NewSize              = 11010048 (10.5MB)
    MaxNewSize           = 178782208 (170.5MB)
    OldSize              = 22544384 (21.5MB)
    NewRatio             = 2
    SurvivorRatio        = 8
    MetaspaceSize        = 21807104 (20.79MB)
    CompressedClassSpaceSize = 1073741824 (1024.0MB)
    MaxMetaspaceSize     = 175921886044415 MB
    G1HeapRegionSize     = 0
```

MaxHeapSize 最大堆内存512MB，是因为读取了limit限制，拿了1/4当上限。  
MaxNewSize  最大新生代170.5MB，是因为读取了默认1/3的堆上限，新生代老年代1：2。  
MetaspaceSize 默认20.79MB,默认值。虽然实际上限很大，但是每次自己扩容还是会产生Full GC，如果程序有很多Class、常量池，还是先定一下大小，比如200M。  
MaxMetaspaceSize 大metspaace上限175PB，因为拿的是虚拟内存的上限。  
GC回收器使用ParallOld，特点是会根据当前内存值进行堆内存的动态大小调节。  

### 5.Concurrent Mode Failure 并发失败导致的GC退化 
单次GC STW耗时4分钟。老年代的剩余空间已经不够同时晋升的区间时触发。 扩大老年代，减少回收比例


### 6.ParNew Allocation Failure 新生代满导致的GC问题
普通的ParNew 回收空间小应该会很快回收完的，当大部分对象在新生代的时候也会触发提前晋升，所以此处的速度应该很块。新生代没有足够的空间分配对象。  
但是实际出现单次ParNew GC花了20s的情况，使用默认ParNew + CMS，原因是对象非常大并且跨越多个块。这意味着所有工作线程都必须多次扫描很长一段路才能找到对象的开头。这就是消耗时间并导致长时间 GC 暂停的原因。

出现该情况时，jamp -heap Pid执行old gc，发现可以回收完，也就是说老年代并没有满。

参考文档： 
https://bugs.openjdk.java.net/browse/JDK-8079274

### 7.Promotion Failure  担保失败，Full GC
Promotion Failure happens when there is no continuous memory space to promote larger object, even though total free memory is large enough. This is problem is called as heap fragmentation. Promotion Failure typically triggers Full GC.

产生原因：老年代没有足够的连续空间分配给晋升的对象（即使总可用内存足够大）。
触发GC类型：Full GC。
解决方法：增加堆内存，特别是新生代内存，尽可能让对象在新生代被回收掉。减少对象的创建，缩短对象的生命周期。


### 8.GCLocker Initiated GC  安全区导致的问题
The GC locker prevents GC from occurring when JNI code is in a critical region. If GC is needed while a thread is in a critical region, then it will allow them to complete, i.e. call the corresponding release function. Other threads will not be permitted to enter a critical region. Once all threads are out of critical regions a GC event will be triggered.

产生原因：如果线程执行在JNI临界区时，刚好需要进行GC，此时GC locker将会阻止GC的发生，同时阻止其他线程进入JNI临界区，直到最后一个线程退出临界区时触发一次GC。
触发GC类型：GCLocker Initiated GC。

参考文档：
https://juejin.cn/post/6844903717595004936
https://shipilev.net/jvm/anatomy-quarks/9-jni-critical-gclocker/
https://www.zhihu.com/question/61361008
https://www.jianshu.com/p/ecc57a81f73c
https://bugs.openjdk.java.net/browse/JDK-8048556
https://tech.meituan.com/2020/11/12/java-9-cms-gc.html








