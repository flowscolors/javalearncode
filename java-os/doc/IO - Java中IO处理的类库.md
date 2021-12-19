
首先Java的IO有好几套，一开始是InputStream，OutputStream那一套，这是面向比特流的。  
很快在在JDK1.1又添加了InputStreamReader，OutputStreamWriter这一套，这是面向字符流的。   
在JDK1.4加入了一整套nio的库，其中又有Buffer ，Channel，等新概念，以及还有新的文件类Path。
但是依旧是很难用。也许终于是忍无可忍，JDK终于开始提供一些稍微封装的工具类，   
如1.5添加了Scanner，1.6添加了Console，1.7又加入了Paths，Files，1.8又加入了Stream相关的接口。 

总的来说好几套设计，有的底层，有的封装，有的同步，有的异步，会用还是不难，重点是分清各种不同的理念，分别理解。

## byte
byte[]

ByteBuf

ByteBuffer
ByteBuffer.clear
ByteBuffer.put
ByteBuffer.flip

InetSocketAddress

## socket
Socket
socket.getOutputStream().write()
socket.getOutputStream().flush()
socket.getInputStream().read()  

ServerSocket
Socket socket = serverSocket.accept();

SocketChannel

ServerSocketChannel

AsynchronousSocketChannel
AsynchronousSocketChannel.open
AsynchronousSocketChannel.connect
AsynchronousSocketChannel.read
AsynchronousSocketChannel.write
AsynchronousSocketChannel.get
AsynchronousSocketChannel.accept

## handle
CompletionHandler<> 


## netty
EventLoopGroup

Bootstrap

ServerBootstrap

ChannelFuture

SimpleChannelInboundHandler

ChannelInboundHandlerAdapter