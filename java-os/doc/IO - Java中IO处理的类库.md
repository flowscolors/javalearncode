
首先Java的IO有好几套，一开始是InputStream，OutputStream那一套，这是面向比特流的。  
很快在在JDK1.1又添加了InputStreamReader，OutputStreamWriter这一套，这是面向字符流的。   
在JDK1.4加入了一整套nio的库，其中又有Buffer ，Channel，等新概念，以及还有新的文件类Path。
但是依旧是很难用。也许终于是忍无可忍，JDK终于开始提供一些稍微封装的工具类，   
如1.5添加了Scanner，1.6添加了Console，1.7又加入了Paths，Files，1.8又加入了Stream相关的接口。 

总的来说好几套设计，有的底层，有的封装，有的同步，有的异步，会用还是不难，重点是分清各种不同的理念，分别理解。/

如果我们把1.4以前的阻塞IO称为OIO，1.4以后的非阻塞IO成为NIO，则在Java里有以下区别。
1.OIO是面向流的，无论是字节流还是字符流。NIO是面向缓冲区的。所以在OIO中我们无法随意改变读取指针的位置，而在NIO中则可以。
2.OIO的操作都是阻塞的，NIO操作是非阻塞的，无数据可以返回失败。
3.OIO没有选择器（selector概念），NIO有，所以NIO需要OS支持。

参考文档：
https://www.zhihu.com/question/67535292


## OIO
### byte
byte[]

ByteBuf


InetSocketAddress

### socket
Socket
socket.getOutputStream().write()
socket.getOutputStream().flush()
socket.getInputStream().read()  

ServerSocket
Socket socket = serverSocket.accept();

SocketChannel

### ServerSocket
ServerSocketChannel

AsynchronousSocketChannel
AsynchronousSocketChannel.open
AsynchronousSocketChannel.connect
AsynchronousSocketChannel.read
AsynchronousSocketChannel.write
AsynchronousSocketChannel.get
AsynchronousSocketChannel.accept

## Java NIO
核心三大概念：Channel（通道）、Buffer（缓冲区）、Selector（选择器）。

### Buffer
做为一个数据结构是数组的内存块，提供了一组方法来有效访问。java.nio.Buffer 做为一个抽象类，提供了fianl的读取、写入方法。
在NIO中有8种缓冲区分类，分别是ByteBuffer、CharBuffer、DoubleBUffer、FloatBuffer、IntBuffer、LongBUffer、ShortBuffer、MappedByteBuffer。最常用的是第一种。
抽象类中没有定义存储缓冲区，即数组，是在每个子类中定义的。ByteBuffer就有一个final byte[] hb.
除了实际缓冲区，还有一些重要通用属性在抽象类中定义，如capacity（容量）、position（位移）、limit（读写限制）


ByteBuffer
ByteBuffer.allocate()  为一个缓冲区分配内存空间。
ByteBuffer.put()       把对象写入缓冲区。要求对象和缓冲区类型一致。
ByteBuffer.flip()  读写模式翻转，之后postition、limit含义会进行变化到对应模式。
ByteBuffer.get()   每次从position的位置读取一个数据，并进行相应的缓冲区数据调整。
ByteBuffer.rewind()   重设limit值，使读完的数据可以再读一遍。
ByteBuffer.mark()     将当前position属性保存到mark值中。
ByteBuffer.reset()    将mark值恢复到position属性中。
ByteBuffer.clear()    强制进入写模式，将position清零，并将limit设置为capacity最大容量值。-‘


### Channel
Java中一个Socket连接使用一个Channel来表示，广义来说一个Channel可以表示一个底层的文件描述符，比如硬件设备、文件、网络连接。
并且也是不同实现类实现具体实现。常用的几种有：FileChannel、SocketChannel、ServerSocketChannel、DatagarmChannel。
可以看到通道和缓冲区关系很大，数据总是从通道读到缓冲区，从缓冲区写入到通道。而在这种情况下，但使用着二者，不用Selector也可以，那就实现阻塞IO就完事了。

FileChannel  只能设置为阻塞模式，不能设置为非阻塞模式。
inputstream.getChannel() 从标准输入、输出流、RandomAccessFile中获取channel
channel.read(buf)     从channel中读取数据放到buffer里，一般搭配while循环，读取完channel中所有内容。
channnel.write(buf)   从buffer中读取数据放到channel中，返回写入成功的字节数。注意入参的buf一定要在写模式。
channel.close()       关闭channel
channel.force(true)   在缓冲区写入通道，不可能每次都实时刷新，force方法就是强制刷盘。

SocketChannel   负责连接的数据传输，可用于服务端和客户端
SockerChannel.open()   创建一个套接字传输管道
socketChannel.configureBlocking(false)  设置为非阻塞模式
socketChannel.connect(new InetSocketAddress("IP","Port"))  对服务IP、端口发起连接。 注意非阻塞情况下由于会直接返回，需要不断自旋。
socketChannel.read(buf)    从channel中读取数据放到buffer里,由于是异步，所以需要靠检查返回值进行校验。
socketChannel.write(buf)   从buffer中读取数据放到channel中，同样会有异步的问题。
IOUtil.closeQuietly(socketChannel)   关闭套接字连接。


ServerSocketChannel  负责连接的监听，仅应用于服务端
serverSocketChannel.configureBlocking(false)  设置为非阻塞模式
serverSocketChannel.bind()       server端的监听方法。
serverSocketChannel.read(buf)    从channel中读取数据放到buffer里,由于是异步，所以需要靠检查返回值进行校验。
serverSocketChannel.write(buf)   从buffer中读取数据放到channel中，同样会有异步的问题。
IOUtil.closeQuietly(serverSocketChannel)   关闭套接字连接。

DatagarmChannel    UDP传输方式，只要直到IP、端口就可以发数据了。
DatagarmChannel.open()   创建一个套接字传输管道
datagarmChannel.configureBlocking(false)  设置为非阻塞模式
datagarmChannel.bind()   server端的监听方法。
datagarmChannel.receive(buf)  读取数据
datagarmChannel.send(buf)     写入数据
datagarmChannel.close()       关闭通道

### Selector
选择器的使命就是完成IO的多路复用，主要工作是通道的注册、监听、事件查询。因此选择器提供了特殊的API，可以选出所监控通道已经发生了哪些IO事件。
由于一个选择器可以监控很多通道，所以一个单线程可以处理很多通道。可供选择器监听的通道IO事件类型有以下4种，需要监听多种时使用|：
1.可读 SelectioonKey.OP_READ     比如socketChannel通道有数据可读
2.可写 SelectioonKey.OP_WRITE    比如socketChannel通道有数据可写
3.连接 SelectioonKey.OP_CONNECT  比如socket完成三次握手，会触发一个connect事件。
4.接收 SelectioonKey.OP_ACCEPT   比如serverSocket监听到一个新连接到来。
并不是所以Channel都可以被Selector的，需要继承SelectableChannel类。

Selector.open()   获取一个Selector实例
socketChannel.register(selector,SelectionKey.OP_ACCEPT)  通过channel的注册方法，把channel注册到selector上，选择监听特定的事件。注册到选择器的channel必须是非阻塞模式。
selector.select() 获取对应的selector事件，执行完该方法后，数据会被存到该选择器的selectedKeys数组中，直接获取即可使用。 


## Netty
Netty是一个Java NIO的客户端/服务器框架，是一个为了快速开发可维护的高性能、高拓展的网络服务器、客户端程序而提供的异步事件驱动基础框架和工具。
核心组件：ByteBuf(缓冲区)、NioSocketChannel（通道的一种）、NioEventLoopGroup（反应器的一种）、ChannelInboundHandler（Handler处理器的一种）、ServerBootStrap（服务引导类）
通道和反应器是一个相互对应的操作，对应于NioSocketChannel通道、Netty的反应器类为NioEventLoop

### Bootstrap
Bootstrap

ServerBootstrap

EventLoopGroup Netty使用EventLoopGroup来实现多线程版本的Reactor模式，默认EventLoopGroup内部线程数量为最大可用CPU处理器数目的2倍。
假设电脑为4核CPU，则内部会启动8个EventLoop，即8个子反应器实例。
### Channel
ChannelOption

ChannelFuture

### Handler
SimpleChannelInboundHandler

ChannelInboundHandlerAdapter

ChannelOutboundHandlerAdapter

CompletionHandler<> 


### Pipline


### ByteBuf
相较于Java NIO的ByteBuffer，ByteBuf的优势如下：
1.Pooling 池化，减少内存复制和GC，提高了效率。
2.复合缓冲区类型，支持零COPY。
3.不需要调用filp()方法切换读写模式。
4.可拓展性好。
5.可自定义缓冲区类型。
6.读取和写入索引分开。
7.方法的链式调用。
8.可以进行引用计数，方便重复使用。

ByteBuf缓冲区的类型有Heap ByteBuf、Direct ByteBuf、CompositeBuffer（多个缓冲区组合）


* 粘包与半包
粘包，接收端收到了一个ByteBuf，包含了发送端的多个ByteBuf，发送端的多个ByteBuf在接受端“粘”在了一起。
半包，Receiver将Sender的一个ByteBuf拆开了收，收到多个破碎的包。

两者都不是一次正常的ByteBuf缓冲区接受，产生原因是OS的TCP内核缓冲区的影响。而解决办法就是Netty做为用户层对BtyeBuf进行二次组装。

### Decoder、Encoder
Netty从Java通道读取ByteBuf二进制数据，传入Netty通道的流水线，随后开始入站处理。而将ByteBuf二进制类型解码成Java POJO对象，这需要Netty进行编码、解码操作。

* 序列化与反序列化
那么Java POJO转换成二进制字节数组就是序列化、反序列化的工作了。
可以转成json的二进制字节数组，开源类库有FastJson、Gson、Jackson。

可以使用Protobuf协议通信，需要使用预先定义的Message数据结构将实际的传输数据进行打包。Netty默认支持Protobuf的编码和解码，内置了一套基础的Protobuf编码解码器。


### Zero Copy
注意Netty的零拷贝其实主要是JVM层级的优化，可以把数据基于用户层级直接从一个Socket传输到另一个Socket，并不是OS级别的优化。
通过CompositeByteBuf实现零拷贝


通过warp操作实现零拷贝



