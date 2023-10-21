
Q:ISO七层模型？
A:物理层（网线）、数据链路层（MAC）、网络层（IP）、传输层（TCP）、会话层、表示层、应用层。

Q:详细描述TCP三次握手和四次挥手的过程？
第一次，client首先发一个syn帧，进入SYN_SENT状态。
第二次，server端收到syn帧，返回syn+ack帧，server端进入SYN_RCVD状态。
第三次，client端收到syn+ack帧，client端进入ESTABLISHED，表示自己方法的连接建立成功。开始数据传输，server端收到之后进入ESTABLISHED。

第一次，client首先发一个Fin包，同时发Ack和Seq，发送完client进入FIN_WAIT_1,表示client没有数据要发送了。
第二次，server端发一个ACK包，表示知道client端结束发送了，server端切换成CLOSED_WAIT状态。此时client端收到ACK包，状态从Fin_WAIT_1切换到FIN_WAIT_2
第三次，server端发送Fin包+ACK包，表示server端的数据发送完了，此时Server端进入LAST_ACK状态。
第四次，client端收到Server端的FIN包，回一个ACK表示知道Server端结束发送了。Server端收到该ACK进入CLOSED，client端需要等2MSL再进入CLOSED，2MSL之间是TIME_WAIT。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/TCP-挥手握手.png)

Q:三次握手为什么不能两次?
A:两次握手，server端在发出ack+seq的时候就认为连接已经建立，可以开始通讯了，而如果这个返回包在网络中被丢弃了，server端已经开始发送包，而client会一直等ack，两边就死锁了。

Q:四次挥手中TIME_WAIT状态存在的目的是什么?
A:因为如果Server端真正关闭了，则最后的包肯定是没有返回的，于是只能靠等一个来回。没有消息返回就是正确的，就可以真正CLSOED了。
用来保证可靠的实现tcp全双工连接的终止，如果客户端最后的FIn+ack没有被收到，服务端会一直在last-ack，此时服务端会重试发Fin包，如果没有2MSL的Time-Wait，客户端的连接就直接关闭了。
允许由于网络阻塞导致的客户端重复信息在本地连接中废掉，而不会进入下一次连接。等了2MSL，相同端口的新TCP连接的报文需要2MSL才会出现在网络中，而此时因为过了2MSL，旧连接的所有数据报文都在网络中消失了。

Q:TCP连接的11种状态？
(1) LISTEN：等待从任何远端TCP和端口的连接请求。
(2) SYN_SENT：发送完一个连接请求后等待一个匹配的连接。
(3) SYN_RCVD：发送连接请求并且接受到匹配的连接请求，等待连接请求确认。
(4) ESTABLISHED：表示一个打开的连接，接受到的数据可以被投递给用户。数据传输的正常情况。
(5) FIN_WAIT_1:已发送Fin包，等待远程TCP的ACK回报，等待远程TCP连接终止请求或者等待连接终止的ACK。
(6) FIN_WAIT_2:等待远程TCP连接终止请求，等待对端Fin包。
(7) TIME_WAIT: 等待2MSL确保对端收到Fin请求。保证可靠的实现tcp全双工连接的终止，允许由于网络阻塞导致的客户端重复信息在本地连接中废掉，而不会进入下一次连接。
(8) CLOSING:等待远程TCP的连接终止请求。
(9) CLSOE_WAIT:等待本地用户的连接终止请求，等待本地发出Fin包。
(10) LAST_ACK:已发出Fin包，等待最后的Fin包的Ack。
(11) CLOSED:连接已关闭，不在连接状态（方便描述的状态，实际不存在）。


Q:什么情况下会出现很多的FIN_WAIT_1？
A:客户端发了Fin包，但是一直没收到对端的ACK。服务端返回时间较长，或者程序错误根本没返回会出现该情况。

Q:什么情况下会出现很多的TIME_WAIT？
A:很多连接被回收，已经进入了正常的关闭流程，在等2MSL了。

Q:什么情况下会出现很多的CLSOE_WAIT？
A:服务端收到Fin包，自己在往客户端发消息一直没发完，服务端没有发出Fin包。服务端的返回时间比较长，比如一个接口需要30s，而调用方20s就超时了，于是客户端来断开连接，于是服务端就会close_wait。
参考文档：https://www.cnblogs.com/grey-wolf/p/10936657.html

Q:TCP是通过什么机制保障可靠性的？
A:校验：校验数据包。序号：对seq。确认：对ACK。重传：超时重传和冗余ACK重传。拥塞控制。流量控制。连接管理：三次握手、四次挥手。

Q:什么时候发RST包？
A:RST标示复位、用来异常的关闭连接。是一种异常关闭导致的包，普通showdown、close都只会发Fin包。以下有几种会触发的情况：
1.  建立连接的SYN到达某端口，但是该端口上没有正在 监听的服务。
2. TCP收到了一个根本不存在的连接上的包。
3. 请求超时。 使用setsockopt的SO_RCVTIMEO选项设置recv的超时时间。接收数据超时时，会发送RST包。

Q:linux最多可以建立多少个tcp连接，client端，server端，超过了怎么办？
A:理论上协议层面上限是无限的，因为TCP连接是五元组（源地址，源端口，协议，目的地址，目的端口）。client端最大65535，server端无限，因为源地址可以无限。
所有上限就变成了OS级别的文件描述符最大打开数的限制。

Q:说一说TCP的拥塞控制？
A:拥塞控制是作用于网络的，它是防止过多的数据注入到网络中，避免出现网络负载过大的情况。因为路由器在数据包满的情况下会直接丢弃新的包，如果TCP直接重传，则会更加加重网络拥塞。  
解决该方法的数据结构就是拥塞窗口，由发送方维持一个叫做拥塞窗口cwnd（congestion window）的状态变量。拥塞窗口的大小取决于网络的拥塞程度，并且动态地在变化。 发送窗口取拥塞窗口和接收端窗口的最小值，避免发送接收端窗口还大的数据。
对应实际的慢启动，拥塞避免算法。慢启动不会一开始发送大量数据包，而是不断试探，由小到大增加拥塞窗口范围，每次倍增。拥塞避免就是线性算法，发现网络阻塞了，则更新ssthresh阈值（当前窗口一半），并cwnd置1，重新开始慢启动。于是多轮循环后就能找到合适的ssthresh阈值。

Q:说一声TCP的流量控制？
A:一种传输时的控制传输速率的方法，类似消息队列pull push模式的第三种调节方法，背压。TCP的流量控制也正是基于此，首先TCP的数据传输窗口是可变的，也即滑动窗口。
滑动窗口的本质是描述接受方的TCP缓冲区能接受多少数据，发送方根据该数据来计算最多发送数据的长度。
客户端、server端首次通信时发送的数据长度取决于链路带宽，之后的发送数据长度由server端回的ack+即时窗口大小（revicer windows）。

参考文档：
https://juejin.cn/post/6844904073611722760
https://draveness.me/whys-the-design-tcp-performance/

---
Q:Socket了解吗？
A:Socket，做为操作系统提供的名为套接字的组件，属于数据传输用的软件设备。对应c的代码为 int socket(int domain, int type, int protocol);
在c中使用Socket做服务端有4步，socket函数创建socket、bind函数分配IP地址和端口、listen函数转化为socket为可接受请求状态、调用accept函数处理连接请求。
在c中使用Socket做客户端有2步，socket函数创建socket、调用connect函数往服务端发请求。
Java直接把Socket分为了Socket和ServerSocket。
在Linux中，socket操作和文件操作没有区别。Windows则和Linux不同，是区分socket和文件的。

Q:tcp和udp的实现区别
A:设计理念就不同，导致最后使用方式、协议报文格式、包头结构（TCP包头20字节，UDP 8字节）都不同。TCP面向连接，保证通信的可靠性。UDP是一个非连接的协议，只是尽可能去发送包。
TCP保证数据正确性，UDP可能丢包。TCP保证数据有序性，UDP不保证。TCP不支持广播，UDP支持广播。

Q:怎么理解用户态，内核态，为什么要分级别，有几种转换的方式，怎么转换的，转换失败怎么办
A:因为OS的系统调用耗时长、影响大。出于安全和效率的考虑设计了用户态的程序。转换方式是进行系统调用即可。而系统调用开销大部分是因为：
上层应用使用软件中断触发的系统调用需要保存堆栈和返回地址等信息，还要在中断描述表中查找系统调用的响应函数。

Q:虚拟内存，虚拟地址和物理地址怎么转换，内存分段，内存分页，优缺点
A:因为OS级别的内存使用是昂贵且珍惜的，为了让应用安全、高效、可拓展的使用内存，有了虚拟内存。
虚拟内存是操作系统物理内存和进程之间的中间层，它为进程隐藏了物理内存这一概念，为进程提供了更加简洁和易用的接口以及更加复杂的功能。
进程持有的虚拟地址（Virtual Address）会经过内存管理单元（Memory Mangament Unit）的转换变成物理地址2，然后再通过物理地址访问内存。让程序以为有很大很快的内存。

Linux 会将物理的随机读取内存（Random Access Memory、RAM）按页分割成 4KB 大小的内存块。如果应用访问了虚拟内存中没有的地址，就会触发缺页中断，MMU会把硬盘中数据加载到虚拟内存中。4
也正是虚拟内存为每个运行的进程提供独立的内存空间，造成了一种每个进程的内存都是独立的假象。在 64 位的操作系统上，每个进程都会拥有 256 TiB 的内存空间。

参考文档：https://draveness.me/whys-the-design-os-virtual-memory/

---
Q:客户端访问url到服务器，整个过程会经历哪些?
A:如果是域名访问，先走域名解析的流程，先访问本地缓存、再依次去各级DNS服务器获取IP.
  拿到IP后浏览器先进行访问，获取到静态资源，一般这是从CDN中获取，浏览器渲染完成后加载成页面。
  涉及到后端访问的程序会有对后端的调用，这时候一般会有TCP建链，然后HTTP请求。

Q:描述HTTPS和HTTP的区别？
A:HTTPS进行了加密。

Q:HTTP中GET和POST的区别？
1.Get请求的数据可以放在URL里，POST则一定需要放在body里。
2.接上，URL长度是有限制的，body则一般无限制。
3.POST传输更加安全。

Q:HTTP连接复用机制？
HTTP连接复用本质上是承载HTTP报文的传输层TCP连接的复用。http1.1，底层的TCP默认是持久连接 Connection:Keep-Alive，只是如果前一个请求尚未完成，还是要建立新的连接的。
当然由于连接复用，不会在TCP级别上发FIn包，所有客户端需要通过返回值Header中的Content-Length来确定自己需要接受的字节，从而确认数据已接受。

---
Q:epoll 和 Selector 有什么区别？
A:一个是操作系统的函数，一个是Java提供的封装类。Selector中使用了epoll去完成对事件的收集。

Q:I/O多路复用中select/poll/epoll的区别？
A:三者都是Linux下对IO多路复用模式的具体实现，按照时间分别是1983、1997、2002.所以肯定是epoll功能和性能最好。具体而言：
select的问题：1.select可支持的文件描述符有上限，取决于sizeOf(fd_set). 2.文件描述符使用后再次使用需要自己置位。 3.文件描述符是从用户态copy到内核态的。 4.select返回后还要再次查询才能知道准备好的。
poll有自己的pollfd结构体，所以每次置位置自己结构体内的字段即可，解决了select重新置位的问题。且由于不依赖bitmaps，所以没了上限的限制。解决1、2.
epoll采用内存映射机制，直接将就绪队列通过MMAP映射到用户态，避免了内存拷贝的额外开销，节约系统资源。并且epoll的文件状态改变时会被放入一个就绪队列，从而结果返回就绪队列，于是返回的fb数组就是准备好的数组而非全部数组，所以减少了遍历查询的时间。解决3、4.

Q:说一下四种IO模型
A:阻塞/非阻塞，同步/异步两两组合。同步阻塞，read/write 默认模型。同步非阻塞，read/write nonblock。异步阻塞，Select、Poll、Epoll，可以自己封装。异步非阻塞，AIO，需要OS支持。
这里select、poll、epoll到底是同步还是异步不同的人有不同观点，如果你从操作系统角度，这绝对是个同步的操作。而如果是应用层来看，请求发来之后没有完成而是先返回一个值，则是异步的概念。或者说你同步/异步的对象是客户端还是服务端。
而select、poll、epoll会阻塞在自己的本身的系统调用上，而不会阻塞在真正的I/O系统调用如recvfrom之上。

Q:selete，poll，epoll？
A:三者都是Linux下对IO多路复用模式的具体实现，按照时间分别是1983、1997、2002.所以肯定是epoll功能和性能最好。具体而言：
select的问题：1.select可支持的文件描述符有上限，取决于sizeOf(fd_set). 2.文件描述符使用后再次使用需要自己置位。 3.文件描述符是从用户态copy到内核态的。 4.select返回后还要再次查询才能知道准备好的。
poll有自己的pollfd结构体，所以每次置位置自己结构体内的字段即可，解决了select重新置位的问题。且由于不依赖bitmaps，所以没了上限的限制。解决1、2.
epoll采用内存映射机制，直接将就绪队列通过MMAP映射到用户态，避免了内存拷贝的额外开销，节约系统资源。并且epoll的文件状态改变时会被放入一个就绪队列，从而结果返回就绪队列，于是返回的fb数组就是准备好的数组而非全部数组，所以减少了遍历查询的时间。解决3、4.

Q:epoll的结构，怎么注册事件，et和lt模式
```
struct epoll_event
{
    __uint32_t events;
    epoll_data_t data;
}

typedef union epoll_data
{
    void * ptr;
    int fd;
    __uint32_t u32;
    __uint64_t u64;
} epoll_data_t;
```

Q:同步阻塞、同步非阻塞、异步的区别？
A:阻塞、非阻塞是指该次IO操作会不会阻塞。异步指由OS发起调用。

Q:select、poll、eopll的区别？
A:三者都是Linux下对IO多路复用模式的具体实现，按照时间分别是1983、1997、2002.所以肯定是epoll功能和性能最好。具体而言：
selector的实现基于bitmaps，所以最大长度是1024.文件描述符使用后再次使用需要自己置位。文件描述符是从用户态copy到内核态的。select返回后还要再次查询才能知道准备好的。
poll有自己的pollfd结构体，所以每次置位置自己结构体内的字段即可，解决了select重新置位的问题。且由于不依赖bitmaps，所以没了1024的限制。
epoll的fd在用户态和内核态之间，所以无需copy，节约系统资源。并且它返回的fb数组就是准备好的数组而非全部数组，所以减少了遍历查询的时间。

Q:java NIO与BIO的区别？
A:BIO不论单线程还是多线程还是线程池都有阻塞的问题。NIO最大的好处是缓冲区数据没准备好就直接返回。

Q:reactor线程模型是什么?
A:基于Selector、Poll、Epoll的reactor模型，是IO多路复用模型的一种实现，主要由reactor和handler组成，由单线程进行事件分发，每个事件有一个handle，执行handler即完成对应操作。

Q:为什么Tomcat的NIO模式中的serverSocket使用了阻塞连接，serverSock.configureBlocking(true); ？
A:对于Reactor模式中，我们需要重点关心的是连接建立后获得的与客户端交互的那个socket，他的操作必须是非阻塞的。而accept()接受请求的scoket则可以分配成非阻塞的，用来减少读取繁忙。
如果设置 ServerSocketChannel 成为非阻塞则将导致读取繁忙 - 即一个线程将不断轮询传入的连接，因为非阻塞模式下的 accept() 可能返回 null。

参考文档：
https://stackoverflow.com/questions/23168910/why-tomcats-non-blocking-connector-is-using-a-blocking-socket
http://tutorials.jenkov.com/java-nio/server-socket-channel.html

Q:如何使用netty替换springboot默认的tomcat容器？
A:删除tomcat相关依赖，引入netty，使用netty handler将servlet request交给dispatcherServlet处理，保证Spring MVC的正常启动。
参考文档：https://www.cnblogs.com/grey-wolf/p/12017818.html

---
Q:Netty服务器程序使用步骤？
1.创建两个线程组boss、work，分别用于接受客户端连接和处理已经接受的连接。 
2.使用ServerBootstarp 服务启动类创建对象，并配置一系列启动参数。
3.当boss线程把接受的连接注册到Work线程中后，需要交给连接初始化消息处理链handler。
4.编写业务处理handler链，实现请求的消息处理。
5.绑定端口。

Q：Netty对Java NIO的改进？
1.做为异步网络处理框架，在Java自带Future基础上添加了Promise机制，更方便异步编程。
2.同为对epoll模型的封装，JDK的epoll模型是水平触发，netty自己采用JNI重写实现了边缘触发。

Q:什么是粘包、半包，怎么解决？
A:在使用Java NIO进行TCP网络通信时，会由于TCP层的拆包导致传了多个包或者半个包，解决办法就是Netty使用了编码器和解码器，去判断字节中是否有换行符、判断每个消息应该有的长度、固定长度解码器。

参考文档：https://draveness.me/whys-the-design-tcp-message-frame/