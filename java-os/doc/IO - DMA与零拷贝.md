

NIC  网卡

DMA  Direct Memory Access，直接内存存取。一种文件传输技术，可以把网卡的数据传输到内核缓冲区。
从前把数据从硬件（网卡、磁盘）读写到内核空间也是CPU来做，现在有了DMA，CPU直接通知DMA做就可以。但是内核空间和用户空间的操作还是得CPU做。

零拷贝  零拷贝(Zero-Copy)用于在数据读写过程中减少不需要的CPU拷贝，CPU资源有限，减少它的负担自然可以提高处理效率。
把同属于内核空间的SOcket缓冲区内容和内核缓冲区内容直接进行拷贝，不过用户空间。可以通过mmap()+write()函数实现，或使用sendfile()函数实现。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/传统IO流程.jpg)

* 为什么Linux默认的Socket是同步阻塞的？
基于数据收发的基本原理，系统利用阻塞降低了CPU消耗。但是低效。
因为阻塞的线程是不占CPU的。而从Socket收数据是毫秒级，用户态真正操作的纳秒级，所以用户线程阻塞，CPU消耗降低。

* 为什么Java提供了零拷贝的API，但Java中没有全部使用零拷贝？
如果用户需要在传输过程中对数据进行加工（如加密），则该场景不适合使用零拷贝。

参考文档：
https://zhuanlan.zhihu.com/p/447638676
https://zhuanlan.zhihu.com/p/442343024
https://www.cnblogs.com/z-sm/p/6547709.html