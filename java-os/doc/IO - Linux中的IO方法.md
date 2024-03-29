
## IO读写原理
无论是基于Socket的网络读写，还是基于文件的读写，都属于对输入input、输出output的处理，简称为IO读写。

用户进行IO读写，基本都需要用到read、write两大系统调用，不同操作系统可能名称不同，但是功能是一样的。

PS：read系统调用，并不是直接把数据从物理设备读到内存。write系统调用也并不是直接把数据从内存写到物理设备。两者都是把数据复制到内核缓冲区。
read系统调用，是把数据从内核缓冲区复制到进程缓冲区。write系统调用，是把进程缓冲区的数据复制到内核缓冲区。
两者都不负载内核缓冲区和磁盘的交互，这步由操作系统完成。于是就由了阻塞、非阻塞的区别，即是否等待缓冲区数据处理好。

PS:缓冲区的存在是为了减少频繁的系统IO调用。比较系统调用及其耗时间和性能。所以因为进程缓冲区的存在，用户的IO操作并不是实际的IO，而是在读写自己的进程缓冲区。

首先看看一个典型Java 服务端处理网络请求的典型过程：
（1）客户端请求
Linux通过网卡，读取客户端的请求数据，将数据读取到内核缓冲区。
（2）获取请求数据
服务器从内核缓冲区读取数据到Java进程缓冲区。
（1）服务器端业务处理
Java服务端在自己的用户空间中，处理客户端的请求。
（2）服务器端返回数据
Java服务端已构建好的响应，从用户缓冲区写入系统缓冲区。
（3）发送给客户端
Linux内核通过网络 I/O ，将内核缓冲区中的数据，写入网卡，网卡通过底层的通讯协议，会将数据发送给目标客户端。

## 4种IO模型
Linux IO模式下有同步、异步、阻塞、非阻塞四个指标。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/Linux IO模式.png)

一般将网络消息是否有返回结果作为同步或异步的区分标准：
同步：应用程序直接参与IO读写操作，并等待消息响应结果。
异步：所有IO读写上交给OS操作，不等待消息响应结果，程序只需要等待通知。

阻塞和菲阻塞是指IO阶段的IO读写操作是否是阻塞：
阻塞：往往需要等待缓冲区的数据准备好才区处理，否则一直等待。
非阻塞：当进程访问数据缓存区时，如果数据没有准备好则直接返回，不会等待，如果数据已经准备好，则也直接返回。

所以我们常说的select、poll、epoll都属于异步阻塞，只实现了异步，实际IO还是阻塞，所以叫他们IO多路复用。
IO多路复用的特点是通过一个机制，使用一个单独的线程同时等待多个文件描述符

首先在java逻辑层使用代码实现客户端轮询的逻辑，肯定是不如底层OS的逻辑调用。  
NIO使用了操作系统底层的轮询系统调用 select/epoll（windows:select linux:epoll） 

参考文档：
https://zhuanlan.zhihu.com/p/447638676
https://zhuanlan.zhihu.com/p/435734598

## 同步阻塞IO
默认的read、write都是同步阻塞IO，需要等数据从物理设备准备到内核空间加载完，read、write才会继续下去，进行内核空间到用户空间的数据copy。


## IO多路复用
一般我们认为IO多路复用是一种同步非阻塞IO，select、poll、epoll都是基于此思想的具体实现。同步指轮询操作是用户发起，非阻塞是指轮询的Socket一般都设置为非阻塞。
为了提高性能，操作系统引入了一种系统调用，专门用于查询IO文件描述符的就绪状态。通过该系统调用，一个用户进程可以监视多个文件描述符，一旦文件描述符就绪，用户线程就可以对其进行相应的IO调用。
所以实际IO多路复用涉及两种系统调用，一种是read、write这种实际IO操作，一种是select、epoll这种就绪查询系统调用。
注意这种情况下也是需要进行轮询的，负责select、epoll状态查询的线程需要不断轮询，去不断找到IO就绪，可以进行IO操作的Socket。但是好处就是只需要一个线程就能轮询上千个Socket。
### select
假设有A、B、C、D、E五个连接同时连接服务器，Java程序将会遍历这五个连接，轮询每个连接，获取各自数据准备情况.
但是我们写的Java程序其本质在轮询每个Socket的时候也需要去调用系统函数，那么轮询一次调用一次，会造成不必要的上下文切换开销。

而Select会将5个请求从用户态复制一份到内核态，在内核态空间直接判断每个请求是否准备好了数据，完全避免频繁上下文切换。
如果select没有查询到到有数据的请求，那么将会一直阻塞（是的，select是一个阻塞函数）。也即当进行IO就绪事件的轮询时，发起查询的这个用户线程是阻塞的.
如果有一个或者多个请求已经准备好数据了，那么select将会先将有数据的文件描述符置位，然后select返回。返回后通过遍历查看哪个请求有数据。

select的缺点：

1.底层存储依赖bitmap，处理的请求是有上限的，为1024。

2.文件描述符是会置位的，所以如果当被置位的文件描述符需要重新使用时，是需要重新赋空值的。

3.fd（文件描述符）从用户态拷贝到内核态仍然有一笔开销。

4.select返回后还要再次遍历，来获知是哪一个请求有数据。

### poll
poll的工作原理和select很像，先来看一段poll内部使用的一个结构体。
```text
struct pollfd{
    int fd;
    short events;
    short revents;
}
```
poll同样会将所有的请求拷贝到内核态，和select一样，poll同样是一个阻塞函数，当一个或多个请求有数据的时候，也同样会进行置位，
但是它置位的是结构体pollfd中的events或者revents置位，而不是对fd本身进行置位，所以在下一次使用的时候不需要再进行重新赋空值的操作。
poll内部存储不依赖bitmap，而是使用pollfd数组的这样一个数据结构，数组的大小肯定是大于1024的。解决了select 1、2两点的缺点。

### epoll
epoll是最新的一种多路IO复用的函数。这里只说说它的特点。

epoll和上述两个函数最大的不同是，它的fd是共享在用户态和内核态之间的，所以可以不必进行从用户态到内核态的一个拷贝，这样可以节约系统资源；
另外，在select和poll中，如果某个请求的数据已经准备好，它们会将所有的请求都返回，供程序去遍历查看哪个请求存在数据，但是epoll只会返回存在数据的请求，这是因为epoll在发现某个请求存在数据时，首先会进行一个重排操作，将所有有数据的fd放到最前面的位置，然后返回（返回值为存在数据请求的个数N），那么我们的上层程序就可以不必将所有请求都轮询，而是直接遍历epoll返回的前N个请求，这些请求都是有数据的请求。
也即无内核copy、返回后无需遍历解决了select的3、4两点的缺点。

### 使用相关
1983，socket 发布在 Unix(4.2 BSD)
1983，select 发布在 Unix(4.2 BSD)
1994，Linux的1.0，已经支持socket和select
1997，poll 发布在 Linux 2.1.23
2002，epoll发布在 Linux 2.5.44

1、socket 和 select 是同时发布的。这说明了，select 不是用来代替传统 IO 的。这是两种不同的用法(或模型)，适用于不同的场景。

2、select、poll 和 epoll，这三个“IO 多路复用 API”是相继发布的。这说明了，它们是 IO 多路复用的3个进化版本。因为 API 设计缺陷，无法在不改变 API 的前提下优化内部逻辑。所以用 poll 替代 select，再用 epoll 替代 poll。