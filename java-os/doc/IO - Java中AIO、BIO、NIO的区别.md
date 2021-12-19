
## 1.AIO、BIO、NIO
AIO、BIO、NIO这是一种概念，或者说思想。而每种操作的实际落地是可以有不同的方法。  

* BIO  BIO，Blocking I/O，同步并阻塞I/O处理。经典在于使用while(true){ socket.accept();···}，每次socket收到包后，执行完毕后进行下一个包的接收、处理。

  BIO的使用也是迭代了好几个方式的，从最开始的单线程BIO，每次收到包后，执行对应逻辑，再处理下一个包。到多线程BIO，在收到包后创建多线程、使用线程池去处理包。  
引入多线程是因为除了socket.accept()，还有socket.read()、socket.write()两个包处理函数是要阻塞IO的。而当一个连接在处理IO的时候，系统是阻塞的，则单线程必然堵塞，多线程可以在IO阻塞系统时，让其他线程使用CPU资源。  
当引入线程池以后，由于线程池天然是一个漏斗，可以缓冲一些系统处理不了的连接和请求。所以在单机连接数不高（1000以内）时，这种方案是比较不错的。  
  
  但是以上方案的问题就在于线程是很贵的，所以会有：1.线程创建和销毁成本很高。 2.线程本身占用内存。 3.上下文切换导致CPU负载高。于是在十万、百万级就需要引入新的机制NIO。     
  服务器端在启动后，首先需要等待客户端的连接请求（第一次阻塞），如果没有客户端连接，服务端将一直阻塞等待，然后当客户端连接后，服务器会等待客户端发送数据（第二次阻塞），如果客户端没有发送数据，那么服务端将会一直阻塞等待客户端发送数据。服务端从启动到收到客户端数据的这个过程，将会有两次阻塞的过程。这就是BIO的非常重要的一个特点，BIO会产生两次阻塞，第一次在等待连接时阻塞，第二次在等待数据时阻塞。
  多线程BIO也有一个问题：如果有大量的请求连接到我们的服务器上，但是却不发送消息，而我们的服务器也会为这些不发送消息的请求创建一个单独的线程，造成浪费。
```text
//1.直接在while(true)中执行业务代码
while(true){

socket = accept();

handle(socket)

}

//2.直接在while(true)中每次创建一个线程去做
while(true){
 
socket = accept();
 
new Thread(new Handler(socket).start();
 
}

//3.直接在while(true)中使用一个线程池进行计算
ExecutorService executor = Executors.newFixedThreadPool(3);

while(true){

socket = accept();

executor.submit(new Handler(socket));

}
```

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/BIO-model.png)

* NIO  non-blocking I/O，同步非阻塞。服务器实现模式为一个请求一个线程，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求时才启动一个线程进行处理。。
   NIO需要解决的最根本的问题就是存在于BIO中的两个阻塞，分别是等待连接时的阻塞和等待数据时的阻塞。  
   1.如果单线程服务器在等待数据时阻塞，那么第二个连接请求到来时，服务器是无法响应的。如果是多线程服务器，那么又会有为大量空闲请求产生新线程从而造成线程占用系统资源，线程浪费的情况。
     
     那么我们的问题就转移到，如何让单线程服务器在等待客户端数据到来时，依旧可以接收新的客户端连接请求。单线程服务器接收数据时阻塞，而无法接收新请求的问题，那么其实可以让服务器在等待数据时不进入阻塞状态，问题不就迎刃而解了吗？
     
     方案1：直接使用非阻塞接受，但是问题在于接受非阻塞了，每次处理完都会收下一个接受，不去处理包，包丢了。 方案二：所有socket缓存一个hashmap，每次轮询去处理，每次都轮询所有的连接，100万次则影响很大。  
     方案3：在真实NIO中，并不会在Java层上来进行一个轮询，而是将轮询的这个步骤交给我们的操作系统来进行，他将轮询的那部分代码改为操作系统级别的系统调用（select函数，在linux环境中为epoll），在操作系统级别上调用select函数，主动地去感知有数据的socket。
     方案4：改进的底层函数epoll。epoll是最新的一种多路IO复用的函数。这里只说说它的特点。epoll和上述两个函数最大的不同是，它的fd是共享在用户态和内核态之间的，所以可以不必进行从用户态到内核态的一个拷贝，这样可以节约系统资源；另外，在select和poll中，如果某个请求的数据已经准备好，它们会将所有的请求都返回，供程序去遍历查看哪个请求存在数据，但是epoll只会返回存在数据的请求，这是因为epoll在发现某个请求存在数据时，首先会进行一个重排操作，将所有有数据的fd放到最前面的位置，然后返回（返回值为存在数据请求的个数N），那么我们的上层程序就可以不必将所有请求都轮询，而是直接遍历epoll返回的前N个请求，这些请求都是有数据的请求。
      
   socket主要的读、写、注册和接收函数，在等待就绪阶段都是非阻塞的，真正的I/O操作是同步阻塞的（消耗CPU但性能非常高）。

   所有系统IO都分为两阶段，等待就绪和操作。举例来说，读函数，分为等待系统可读和真正的读；同理，写函数分为等待网卡可以写和真正的写。等待就绪的阻塞是不使用CPU的，是在空等。真正读写操作的阻塞是使用CPU，在真正干活，但是这个速度很快，memory copy，基本忽略不计。    
   
   BIO直接使用socket功能，没做封装，所以在IO时并不知道能不能写、能不能读。NIO则封装了一层，读写函数可以立即返回，这就可以再进行封装，如果一个连接不能读写（socket.read()返回0或者socket.write()返回0），我们可以把这件事记下来，记录的方式通常是在Selector上注册标记位，然后切换到其它就绪的连接（channel）继续进行读写。于是就可以保证真正CPU都用到CPU的计算上。

   NIO使用步骤：
   1.注册当这事件（读就绪、写就绪、有新连接到来）到来的时候所对应的处理器。  
   2.在合适的时机告诉事件选择器：我对这个事件感兴趣。对于写操作，就是写不出去的时候对写事件感兴趣；对于读操作，就是完成连接和系统没有办法承载新读入的数据的时；对于accept，一般是服务器刚启动的时候；而对于connect，一般是connect失败需要重连或者直接异步调用connect的时候。  
   3.用一个死循环选择就绪的事件（如channel=Selector.select()），会执行系统调用，还会阻塞的等待新事件的到来。新事件到来的时候，会在selector上注册标记位，标示可读、可写或者有连接到来。  
   
   注意：select是阻塞的，无论是通过操作系统的通知（epoll）还是不停的轮询(select，poll)，这个函数是阻塞的。所以你可以放心大胆地在一个while(true)里面调用这个函数而不用担心CPU空转。
   以下是最简单的Reactor模式：注册所有感兴趣的事件处理器，单线程轮询选择就绪事件，执行事件处理器。   
```text
interface ChannelHandler{
      void channelReadable(Channel channel);
      void channelWritable(Channel channel);
   }
   class Channel{
     Socket socket;
     Event event;//读，写或者连接
   }

   //IO线程主循环:
   class IoThread extends Thread{
   public void run(){
   Channel channel;
   while(channel=Selector.select()){//选择就绪的事件和对应的连接
      if(channel.event==accept){
         registerNewChannelHandler(channel);//如果是新连接，则注册一个新的读写处理器
      }
      if(channel.event==write){
         getChannelHandler(channel).channelWritable(channel);//如果可以写，则执行写事件
      }
      if(channel.event==read){
          getChannelHandler(channel).channelReadable(channel);//如果可以读，则执行读事件
      }
    }
   }
   Map<Channel，ChannelHandler> handlerMap;//所有channel的对应事件处理器
  }
```
 
![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/NIO-model.png)

* AIO  Async I/O 异步IO，OS需要支持异步IO操作API。

异步的概念和同步相对。当一个异步过程调用发出后，调用者不能立刻得到结果。实际处理这个调用的部件在完成后，通过状态、通知和回调来通知调用者。


对上述三种情况的对比，以socket.read()为例子：
           
对于BIO，执行socket.read()，如果TCP RecvBuffer里没有数据，函数会一直阻塞，直到收到数据，返回读到的数据。

对于NIO，如果TCP RecvBuffer有数据，就把数据从网卡读到内存，并且返回给用户；反之则直接返回0，永远不会阻塞。

对于AIO，不但等待就绪是非阻塞的，就连数据从网卡到内存的过程也是异步的。

换句话说，BIO里用户最关心“我要读”，NIO里用户最关心”我可以读了”，在AIO模型里用户更需要关注的是“读完了”。

再换句话说：

BIO：一个连接一个线程，客户端有连接请求时服务器端就需要启动一个线程进行处理。线程开销大。
伪异步IO：将请求连接放入线程池，一对多，但线程还是很宝贵的资源。

NIO：一个请求一个线程，但客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求时才启动一个线程进行处理。

AIO：一个有效请求一个线程，客户端的I/O请求都是由OS先完成了再通知服务器应用去启动线程进行处理，

BIO是面向流的，NIO是面向缓冲区的；BIO的各种流是阻塞的。而NIO是非阻塞的；BIO的Stream是单向的，而NIO的channel是双向的。

NIO的特点：事件驱动模型、单线程处理多任务、非阻塞I/O，I/O读写不再阻塞，而是返回0、基于block的传输比基于流的传输更高效、更高级的IO函数zero-copy、IO多路复用大大提高了Java网络应用的可伸缩性和实用性。基于Reactor线程模型。

在Reactor模式中，事件分发器等待某个事件或者可应用或个操作的状态发生，事件分发器就把这个事件传给事先注册的事件处理函数或者回调函数，由后者来做实际的读写操作。如在Reactor中实现读：注册读就绪事件和相应的事件处理器、事件分发器等待事件、事件到来，激活分发器，分发器调用事件对应的处理器、事件处理器完成实际的读操作，处理读到的数据，注册新的事件，然后返还控制权。


参考文档：  
https://blog.objectspace.cn/2019/10/22/%E4%BB%8E%E5%AE%9E%E8%B7%B5%E8%A7%92%E5%BA%A6%E9%87%8D%E6%96%B0%E7%90%86%E8%A7%A3BIO%E5%92%8CNIO/#BIO
https://www.cnblogs.com/crazymakercircle/p/13903625.html

## NIO的线程优化模型
多线程。BIO作为peer - connection - peer - thread的某种方案。线程数过多是一个问题。  
|
单线程。NIO的优化方向，首先是把原来的阻塞占用变成了单线程轮询事件，找到可以读写的网络描述符进行读写。除了事件的轮询是阻塞的，剩余IO操作都是纯CPU操作，不需要开启多线程。  
并且由于线程的节约，顺带解决了连接数大的线程上下文切换。处理事件肯定是单线程最好。
|
多线程。在整个NIO的机制（1.事件分发器 2.IO处理器 3.业务线程）里面，还是有一些组件可以并发去做，IO处理器开和CPU数一致的线程，业务线程可以多开些。  
连接的处理和读写的处理通常可以选择分开，这样对于海量连接的注册和读写就可以分发。虽然read()和write()是比较高效无阻塞的函数，但毕竟会占用CPU，如果面对更高的并发则无能为力。  




## 2.NIO的实际使用方法
NIO

实际上的Reactor模式，是基于Java NIO的，在他的基础上，抽象出来两个组件——Reactor和Handler两个组件：

（1）Reactor：负责响应IO事件，当检测到一个新的事件，将其发送给相应的Handler去处理；新的事件包含连接建立就绪、读就绪、写就绪等。

（2）Handler:将自身（handler）与事件绑定，负责事件的处理，完成channel的读入，完成处理业务逻辑后，负责将结果写出channel。

```java
static class Server
    {

        public static void testServer() throws IOException
        {

            // 1、获取Selector选择器
            Selector selector = Selector.open();

            // 2、获取通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 3.设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 4、绑定连接
            serverSocketChannel.bind(new InetSocketAddress(SystemConfig.SOCKET_SERVER_PORT));

            // 5、将通道注册到选择器上,并注册的操作为：“接收”操作
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 6、采用轮询的方式，查询获取“准备就绪”的注册过的操作
            while (selector.select() > 0)
            {
                // 7、获取当前选择器中所有注册的选择键（“已经准备就绪的操作”）
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext())
                {
                    // 8、获取“准备就绪”的时间
                    SelectionKey selectedKey = selectedKeys.next();

                    // 9、判断key是具体的什么事件
                    if (selectedKey.isAcceptable())
                    {
                        // 10、若接受的事件是“接收就绪” 操作,就获取客户端连接
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        // 11、切换为非阻塞模式
                        socketChannel.configureBlocking(false);
                        // 12、将该通道注册到selector选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    else if (selectedKey.isReadable())
                    {
                        // 13、获取该选择器上的“读就绪”状态的通道
                        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();

                        // 14、读取数据
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int length = 0;
                        while ((length = socketChannel.read(byteBuffer)) != -1)
                        {
                            byteBuffer.flip();
                            System.out.println(new String(byteBuffer.array(), 0, length));
                            byteBuffer.clear();
                        }
                        socketChannel.close();
                    }

                    // 15、移除选择键
                    selectedKeys.remove();
                }
            }

            // 7、关闭连接
            serverSocketChannel.close();
        }

        public static void main(String[] args) throws IOException
        {
            testServer();
        }
    }
```

参考文档：  
https://blog.objectspace.cn/2019/10/22/%E4%BB%8E%E5%AE%9E%E8%B7%B5%E8%A7%92%E5%BA%A6%E9%87%8D%E6%96%B0%E7%90%86%E8%A7%A3BIO%E5%92%8CNIO/

## 3.使用一些工具类封装过的IO操作


## NIO的一些坑
Java NIO的一些坑：
JDK原生也有一套网络应用程序API，但是存在一系列问题，主要如下：  
1. Java的Selector对于Linux系统来说，有一个致命限制：同一个channel的select不能被并发的调用。因此，如果有多个I/O线程，必须保证：一个socket只能属于一个IoThread，而一个IoThread可以管理多个socket。

2. 使用NIO != 高性能，当连接数<1000，并发程度不高或者局域网环境下NIO并没有显著的性能优势。

3. 并且NIO并没有完全屏蔽平台差异，它仍然是基于各个操作系统的I/O系统实现的，差异仍然存在。

4. 使用NIO做网络编程构建事件驱动模型并不容易，陷阱重重。NIO的类库和API繁杂，使用麻烦，你需要熟练掌握Selector、ServerSocketChannel、SocketChannel、ByteBuffer等，并且需要其他能力补齐。  

5. 可靠性能力补齐，开发工作量和难度都非常大。例如客户端面临断连重连、网络闪断、半包读写、失败缓存、网络拥塞和异常码流的处理等等，NIO编程的特点是功能开发相对容易，但是可靠性能力补齐工作量和难度都非常大

所以推荐使用成熟的NIO框架，如Netty，MINA等。解决了很多NIO的陷阱，并屏蔽了操作系统的差异，有较好的性能和编程模型。

Netty的对JDK自带的NIO的API进行封装，解决上述问题，主要特点有：

1. 设计优雅 适用于各种传输类型的统一API - 阻塞和非阻塞Socket 基于灵活且可扩展的事件模型，可以清晰地分离关注点 高度可定制的线程模型 - 单线程，一个或多个线程池 真正的无连接数据报套接字支持（自3.1起）

2. 使用方便 详细记录的Javadoc，用户指南和示例 没有其他依赖项，JDK 5（Netty 3.x）或6（Netty 4.x）就足够了

3. 高性能 吞吐量更高，延迟更低 减少资源消耗 最小化不必要的内存复制

4. 安全 完整的SSL / TLS和StartTLS支持

5. 社区活跃，不断更新 社区活跃，版本迭代周期短，发现的BUG可以被及时修复，同时，更多的新功能会被加入

当然也有一些其他的NIO框架，工具类可以使用。比如Commons-IO中的IOUtils

参考文档：  https://www.cnblogs.com/opaljc/archive/2013/05/04/3058692.html 


## Proactor与Reactor
一般情况下，I/O 复用机制需要事件分发器（event dispatcher）。 事件分发器的作用，即将那些读写事件源分发给各读写事件的处理者，就像送快递的在楼下喊: 谁谁谁的快递到了， 快来拿吧！开发人员在开始的时候需要在分发器那里注册感兴趣的事件，并提供相应的处理者（event handler)，或者是回调函数；事件分发器在适当的时候，会将请求的事件分发给这些handler或者回调函数。

涉及到事件分发器的两种模式称为：Reactor和Proactor。 Reactor模式是基于同步I/O的，而Proactor模式是和异步I/O相关的。在Reactor模式中，事件分发器等待某个事件或者可应用或个操作的状态发生（比如文件描述符可读写，或者是socket可读写），事件分发器就把这个事件传给事先注册的事件处理函数或者回调函数，由后者来做实际的读写操作。

而在Proactor模式中，事件处理者（或者代由事件分发器发起）直接发起一个异步读写操作（相当于请求），而实际的工作是由操作系统来完成的。发起时，需要提供的参数包括用于存放读到数据的缓存区、读的数据大小或用于存放外发数据的缓存区，以及这个请求完后的回调函数等信息。事件分发器得知了这个请求，它默默等待这个请求的完成，然后转发完成事件给相应的事件处理者或者回调。举例来说，在Windows上事件处理者投递了一个异步IO操作（称为overlapped技术），事件分发器等IO Complete事件完成。这种异步模式的典型实现是基于操作系统底层异步API的，所以我们可称之为“系统级别”的或者“真正意义上”的异步，因为具体的读写是由操作系统代劳的。

举个例子，将有助于理解Reactor与Proactor二者的差异，以读操作为例（写操作类似）。

在Reactor中实现读
注册读就绪事件和相应的事件处理器。
事件分发器等待事件。
事件到来，激活分发器，分发器调用事件对应的处理器。
事件处理器完成实际的读操作，处理读到的数据，注册新的事件，然后返还控制权。
在Proactor中实现读：
处理器发起异步读操作（注意：操作系统必须支持异步IO）。在这种情况下，处理器无视IO就绪事件，它关注的是完成事件。
事件分发器等待操作完成事件。
在分发器等待过程中，操作系统利用并行的内核线程执行实际的读操作，并将结果数据存入用户自定义缓冲区，最后通知事件分发器读操作完成。
事件分发器呼唤处理器。
事件处理器处理用户自定义缓冲区中的数据，然后启动一个新的异步操作，并将控制权返回事件分发器。
可以看出，两个模式的相同点，都是对某个I/O事件的事件通知（即告诉某个模块，这个I/O操作可以进行或已经完成)。在结构上，两者也有相同点：事件分发器负责提交IO操作（异步)、查询设备是否可操作（同步)，然后当条件满足时，就回调handler；不同点在于，异步情况下（Proactor)，当回调handler时，表示I/O操作已经完成；同步情况下（Reactor)，回调handler时，表示I/O设备可以进行某个操作（can read 或 can write)。

###  Reactor编程的优点和缺点
6.1. 优点
1）响应快，不必为单个同步时间所阻塞，虽然Reactor本身依然是同步的；

2）编程相对简单，可以最大程度的避免复杂的多线程及同步问题，并且避免了多线程/进程的切换开销；

3）可扩展性，可以方便的通过增加Reactor实例个数来充分利用CPU资源；

4）可复用性，reactor框架本身与具体事件处理逻辑无关，具有很高的复用性；

6.2. 缺点
1）相比传统的简单模型，Reactor增加了一定的复杂性，因而有一定的门槛，并且不易于调试。

2）Reactor模式需要底层的Synchronous Event Demultiplexer支持，比如Java中的Selector支持，操作系统的select系统调用支持，如果要自己实现Synchronous Event Demultiplexer可能不会有那么高效。

3） Reactor模式在IO读写数据时还是在同一个线程中实现的，即使使用多个Reactor机制的情况下，那些共享一个Reactor的Channel如果出现一个长时间的数据读写，会影响这个Reactor中其他Channel的相应时间，比如在大文件传输时，IO操作就会影响其他Client的相应时间，因而对这种操作，使用传统的Thread-Per-Connection或许是一个更好的选择，或则此时使用改进版的Reactor模式如Proactor模式。


### 常见面试题
Q:同步阻塞、同步非阻塞、异步的区别？

Q:select、poll、eopll的区别？

Q:java NIO与BIO的区别？

Q:reactor线程模型是什么?




参考文档：  
https://tech.meituan.com/2016/11/04/nio.html
https://www.cnblogs.com/crazymakercircle/p/9833847.html
https://www.cnblogs.com/crazymakercircle/p/13903625.html