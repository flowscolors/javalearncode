
由于netty基于reactor模型，以eventloop方式处理connect，所以可以做到不阻塞。  

当连接池大小设置为1，同时发三个请求过来，三个请求都能被后端接受。


### 自定义ConnectionLimitHandler
但是总有人有特殊需求，如果有人想控制最大连接数，比如连接数3，第4个请求就无法建立连接，netty也提供了自定义的拓展机制connectionlimitHandler  
可以通过自定义一个ConnectionLimitHandler实现连接数限制，然后将其注册到MainReactor上，每次处理连接请求的时候，即可判断是否超过设置的最大连接数，超过则拒绝访问并记录日志。继承ChannelInboundHandlerAdapter并重写channelRead方法，写完相应逻辑后，通过ServerBootstrap注册到MainReactor上。后面请求来的时候就会执行相应的逻辑了。  

参考文档：
https://www.cnblogs.com/liangpiorz/p/15037574.html