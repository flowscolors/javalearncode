
Netty是 一个异步事件驱动的网络应用程序框架，用于快速开发可维护的高性能协议服务器和客户端。Netty是基于nio的，它封装了jdk的nio，让我们使用起来更加方法灵活。

Netty做为底层通信框架，封装了对操作系统Socket的交互、并丰富了其功能性、解决拆包、粘包、分隔符切分、IO模型、心跳、空闲检查、拓展性等问题。

有很多中间件都选择了netty来做底层通信组件，比如 SpringCloud Gateway、Kafka、Dubbo、RocketMQ，就是因为netty已经足够好用，高性能。

### Netty简单使用

由于netty基于reactor模型，以eventloop方式处理connect，所以可以做到不阻塞。  

当连接池大小设置为1，同时发三个请求过来，三个请求都能被后端接受。


### 自定义ConnectionLimitHandler
但是总有人有特殊需求，如果有人想控制最大连接数，比如连接数3，第4个请求就无法建立连接，netty也提供了自定义的拓展机制connectionlimitHandler  
可以通过自定义一个ConnectionLimitHandler实现连接数限制，然后将其注册到MainReactor上，每次处理连接请求的时候，即可判断是否超过设置的最大连接数，超过则拒绝访问并记录日志。继承ChannelInboundHandlerAdapter并重写channelRead方法，写完相应逻辑后，通过ServerBootstrap注册到MainReactor上。后面请求来的时候就会执行相应的逻辑了。  

参考文档：
https://www.cnblogs.com/liangpiorz/p/15037574.html

### Netty组成
Buffer：与Channel进行交互，数据是从Channel读入缓冲区，从缓冲区写入Channel中的

flip方法 ： 反转此缓冲区，将position给limit，然后将position置为0，其实就是切换读写模式

clear方法 ：清除此缓冲区，将position置为0，把capacity的值给limit。

rewind方法 ： 重绕此缓冲区，将position置为0

DirectByteBuffer可减少一次系统空间到用户空间的拷贝。但Buffer创建和销毁的成本更高，不可控，通常会用内存池来提高性能。直接缓冲区主要分配给那些易受基础系统的本机I/O 操作影响的大型、持久的缓冲区。如果数据量比较小的中小应用情况下，可以考虑使用heapBuffer，由JVM进行管理。

Channel：表示 IO 源与目标打开的连接，是双向的，但不能直接访问数据，只能与Buffer 进行交互。通过源码可知，FileChannel的read方法和write方法都导致数据复制了两次！

Selector可使一个单独的线程管理多个Channel，open方法可创建Selector，register方法向多路复用器器注册通道，可以监听的事件类型：读、写、连接、accept。注册事件后会产生一个SelectionKey：它表示SelectableChannel 和Selector 之间的注册关系，wakeup方法：使尚未返回的第一个选择操作立即返回，唤醒的

原因是：注册了新的channel或者事件；channel关闭，取消注册；优先级更高的事件触发（如定时器事件），希望及时处理。

Selector在Linux的实现类是EPollSelectorImpl，委托给EPollArrayWrapper实现，其中三个native方法是对epoll的封装，而EPollSelectorImpl. implRegister方法，通过调用epoll_ctl向epoll实例中注册事件，还将注册的文件描述符(fd)与SelectionKey的对应关系添加到fdToKey中，这个map维护了文件描述符与SelectionKey的映射。

fdToKey有时会变得非常大，因为注册到Selector上的Channel非常多（百万连接）；过期或失效的Channel没有及时关闭。fdToKey总是串行读取的，而读取是在select方法中进行的，该方法是非线程安全的。

Pipe：两个线程之间的单向数据连接，数据会被写到sink通道，从source通道读取

NIO的服务端建立过程：Selector.open()：打开一个Selector；ServerSocketChannel.open()：创建服务端的Channel；bind()：绑定到某个端口上。并配置非阻塞模式；register()：注册Channel和关注的事件到Selector上；select()轮询拿到已经就绪的事件