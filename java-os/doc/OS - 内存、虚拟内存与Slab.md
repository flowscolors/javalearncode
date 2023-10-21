## 1 基本使用
基本的top、free -h命令都是常用的查看内存占用的命令。一般top是看RES 物理内存，free -h看free。
还有ps -aux 等命令可以查看瞬时内存使用。高级一点的Prometheus、Grafana等监控平台可以看。

top命令:
需要注意的是VIRT的使用是可以很大的，单个Java程序的内存可能只有8G，VIRT可以到50G。
VIRT：进程占用的虚拟内存
RES：进程占用的物理内存
SHR：进程使用的共享内存
S：进程的状态。S表示休眠，R表示正在运行，Z表示僵死状态，N表示该进程优先值为负数


free命令:
 total=used+free
 实际内存占用：used-buffers-cached 即 total-free-buffers-cached
 实际可用内存：buffers+cached+free

```
free -h
ps aux | awk '{mem += $6} END {print mem/1024/1024}'
cat /proc/meminfo | grep Slab
vmstat -s
```
  
## 虚拟内存

首先，对于计算机技术而言，"虚拟内存"指的是一种内存管理的技术方式，而不是某种实现或工具，如Swap。虚拟内存做为虚构的内存空间，可以映射到实际的物理空间，从而分配实际内存。

在虚拟内存技术中，硬盘等外部存储介质可以充当虚拟内存地址的临时媒介，所以也有人称该实现为虚拟内存，如Swap。当然也有人认为整个内存地址都是虚拟内存，因为使用的就是虚拟内存，而非实际地址。所以虚拟内存这个词需要整合语境看表达的什么。

最初的操作系统并没有现在那么完善，刚开始的时候，程序是直接装载到物理内存中的。这就导致了下面的一些问题：程序编写困难。修改内存数据导致程序崩溃。

虚拟内存概念的出现就解决了上面的问题，虚拟内存的概念出现后，程序的编写就不再直接操作物理内存了，对每个程序来说，它们就相当于拥有了所有的内存空间，可以随意操作，就不用担心自己操作的内存地址被其他程序占用的问题了。同时，因为程序操作的是虚拟内存地址，这样就不会出现因为修改了其他应用程序内存地址中的数据而导致其他应用程序崩溃的问题了。

所以实际就有了两个内存，真实的内存，即实际的4C 12G的内存。虚拟内存，在64位操作系统上，可用的最大虚拟地址空间有16EB，即大概180亿GB。其实有点像request和limit的关系。
```
void *mem = mmap(0, 4ul * 1024ul * 1024ul * 1024ul * 1024ul,PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS | MAP_NORESERVE,-1, 0);
```
可以用该命令获得4T内存。虚拟内存的使用可以使用pmap -x Pid查看，对应一个java程序，结果大部分是64MB的[anon]，一个进程最多可以有cores*8的arena，64C 512G的物理机，对应 64*8*64M = 32G
> 这是 glibc 在版本 2.10 引入的 arena 新功能导致。CentOS 6/7 的 glibc 大都是 2.12/ 2.17 了，所以都会有这个问题。这个功能对每个进程都分配一个分配一个本地arena来加速多线程的执行。
> 也即每个线程除了占用-Xss的物理内存空间大小，还会占用64MB的虚拟内存，这其中只会有小部分被分配到物理内存。
> 顺便提一句就是虽然默认-Xss是1M，但是实际并不是会7000个线程会占用7G大小，使用NMT查看，大概只有800M左右，平均100K，注意JDK8的NMT有bug，在JDK11修复。
> 因为实际OS并不是直接分配1M，而是根据线程的内容去分配，大部分线程是用不满这1M的。事实上 linux 对物理内存的使用非常的抠门，一开始除了内核栈+thread_info的8K外，只是分配了虚拟内存的线性区，并没有分配实际的物理内存，只有推到最后使用的时候才分配具体的物理内存，即所谓的请求调页。  

但是虚拟内存毕竟是操作系统的逻辑结构，应用程序最终还是要访问物理内存或者磁盘上的内容。应用并不会因为虚拟内存过大而OOM。

参考文档：  
【推荐】https://www.zhihu.com/question/295194595/answer/999804696
https://www.cnblogs.com/seasonsluo/p/java_virt.html
https://xie.infoq.cn/article/b2890eefbbead36c208318eaa
https://www.easyice.cn/archives/341
https://draveness.me/whys-the-design-os-virtual-memory/

## Swap
Swap是上一Part虚拟内存的第二种含义的实现，即"自动交换技术"。交换技术可以让正在或马上运行的程序获得足够的物理内存资源，让不需要或退出运行周期的程序让出占用的物理资源。

并且这个技术由OS直接提供，不需要程序员手动实现。一般会由1.5到3倍物理内存的说法，或者默认，或者为了性能直接关闭。当然你关了Swap也会由虚拟内存使用，但是此时虚拟内存映射到的就全部是物理内存的值了。

## Slab
接下来这个Slab就麻烦，它是真正占用物理内存，无法释放的Slab多了，就会导致操作系统OOM了。下面例子在一台4C 12G机器上。
1.ps aux | awk '{mem += $6} END {print mem/1024/1024}'  所有程序占用总内存是3.8G。free -h，已经占用10+G，只剩170M free了。  

2.查看/porc/meninfo 发现Slab占用约6.8G，其中约6.5G都是SU，无法clean。
```
cat /proc/meminfo | grep Slab
cat /proc/meminfo | grep SR
cat /proc/meminfo | grep SU
```

3.使用slabtop、/proc/slabinfo查看Slab的具体使用情况，这就是麻烦的地方，这里并不会展示Slab被什么线程占用，而是只会展示类型和对象。  
如果是很明显的对象，我们可以推测是什么程序导致，比如ext3_inode_cache和dentry_cache表示大量对文件操作，比如rsync。如果有TCP，那表示有网络链接相关的程序。  
但是如果是kmalloc-2048这种默认创建的，就意味这有程序来往内存申请2MB大小的空间，但是并不知道谁来申请的。

4.不幸中的万幸，Linux kernel自2.6.23之后就已经从Slab进化成Slub了，它自带原生的诊断功能，比Slab更方便。slub debug工具使用。

> Slab Allocation 是Kernel 2.2之后引入的一个内存管理机制，专门用于缓存内核的数据对象，可以理解为一个内核专用的对象池，可以提高系统性能并减少内存碎片。(Kernel 2.6.23之后，SLUB成为了默认的allocator)


解决办法：
1.如果是SReclaimable过多，那可以手工清除 Slab。 echo 2 > /proc/sys/vm/drop_caches 。  
但是这是个治标不治本的办法，因为内存泄漏的进程还在，过会又会占用上，并且如果SReclaimable比较小，SUnreclaim过多，这个方法也没用。  

2.使用内核检查工具追踪。kmemleak、slub debug。

参考文档：  
https://so1n.me/2020/05/28/%E5%86%85%E5%AD%98%E7%BB%9F%E8%AE%A1/
https://www.jianshu.com/p/a7af7c29c9e2
http://linuxperf.com/?p=148
http://linuxperf.com/?p=184
http://www.wowotech.net/memory_management/426.html
实战解决 https://blog.csdn.net/21cnbao/article/details/113792830

## Buddy内存泄漏

Linux内核使用层次化内存管理的方法，每一层解决不同的问题，从下至上的关键部分如下：

* 物理内存管理，主要用于描述内存的布局和属性，主要有Node、Zone和Page三个结构，使内存按照Page为单位来进行管理；
* Buddy内存管理，主要解决外部碎片问题，使用get_free_pages等函数以Page的N次方为单位进行申请释放；
* Slab内存管理，主要解决内部碎片问题，可以按照使用者指定的大小批量申请内存（需要先创建对象缓存池）；
* 内核缓存对象，使用Slab预先分配一些固定大小的缓存，使用kmalloc、vmalloc等函数以字节为单位进行内存申请释放。
接下来，我们首先要看内存是从哪个层次上泄露的（额外说明：还有很多诸如如大页内存，页缓存，块缓存等相关内存管理技术，他们都是从这几个层次里面申请内存，不是关键，这里全部忽略掉）。

```
cat /proc/buddyinfo
cat /proc/buddyinfo | awk '{sum=0;for(i=5;i<=NF;i++) sum+=$i*(2^(i-5))};{total+=sum/256};{print $1 " " $2 " " $3 " " $4 "\t : " sum/256 "M"} END {print "total\t\t\t : " total "M"}'

slabtop -o
cat /proc/slabinfo
```

参考文档:  
https://www.bianchengquan.com/article/507165.html

## Docker中的cpu_set和cpu_share
CPU Manager支持两种Policy，分别为none和static，通过kubelet --cpu-manager-policy设置，默认为none。
none: 为cpu manager的默认值，相当于没有启用cpuset的能力。cpu request对应到cpu share，cpu limit对应到cpu quota。
static: 目前，请设置--cpu-manager-policy=static来启用，kubelet将在Container启动前分配绑定的cpu set，分配时还会考虑cpu topology来提升cpu affinity。

cpu_set : 默认关闭，需要手动开启，kubelet 去指向绑核操作。数目为request中声明的整数大小。
cpu_share : 默认的request值。CFS调度中的权重，一般来说，在条件相同的情况下，cpushare值越高的，将会分得更多的时间片。
cpu_quota ： 默认的limit值。cfs_quota_us/cfs_period_us代表了该容器实际可用的做多的CPU核数。