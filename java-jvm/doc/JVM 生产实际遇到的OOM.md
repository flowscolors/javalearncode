
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

### 1.数据库查询没有limit
一次查询返回数据量太多，直接给后端干崩了，arraylist存不下，元素赋值时不允许。  
一次拉取过多数据做本地缓存。  
总之都是大对象导致的GC。

### 2.内存溢出
一般是启动时不会发现，慢慢增加的，比如:
* hashmap有put，没有remove。导致对象越来越大。
* 

### 3.加载大量文件到内存
比如大量文本、视频，直接全量加载到内存处理。 

### 4.使用默认JVM参数
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
MetaspaceSize 默认20.79MB,默认值。
MaxMetaspaceSize 大metspaace上限175PB，因为拿的是虚拟内存的上限。
GC回收器使用ParallOld，特点是会根据当前内存值进行堆内存的动态大小调节。

### 5.大量反射代码导致老年代对象过多

