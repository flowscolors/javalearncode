

### Netty特点
* 高并发。基于NIO实现。
* 传输快。内存零拷贝、内存池设计、串行化处理读写、protobuf 高性能序列化协议。
* 功能强。预置了多种编解码功能，支持多种主流协议。
* 封装好。封装了很多NIO细节，提供了易于调用的接口。
* 定制能力强。可以通过 ChannelHandler 对通信框架进行灵活地扩展。

Netty高性能场景：
* IO 线程模型：同步非阻塞，用最少的资源做更多的事。
* 内存零拷贝：尽量减少不必要的内存拷贝，实现了更高效率的传输。
* 内存池设计：申请的内存可以重用，主要指直接内存。内部实现是用一颗二叉查找树管理内存分配情况。
* 串形化处理读写：避免使用锁带来的性能开销。
* 高性能序列化协议：支持 protobuf 等高性能序列化协议。