
REST 其实是一种架构模式，常使用Http协议实现。

RPC (Remote Procedure Call) 与REST方式的请求调用，RPC有更好的契约规范，和更好的性能优势。

两者在请求模式上都属于点对点的请求/响应模式。但两者实际涉及思想还是有些区别的。

RPC的思想是把本地函数映射到API，也就是一个API对应一个函数方法，如果本地有一个getUser方法，能通过某种协议让外部调用该方法，至于协议是Socket、Http还是其他协议则不重要。
因此RPC最大的劣势是“紧耦合”，很难在不更改客户端的情况下更改服务，所以RPC的集成交互更偏向内部调用。

常用的RPC框架有阿里的Dubbo、FaceBook的Thrift、Google的gRPC。

