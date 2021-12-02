

Netty是典型的Reactor模型结构，关于Reactor的详尽阐释，本文站在巨人的肩膀上，借助 Doug Lea（就是那位让人无限景仰的大爷）的“Scalable IO in Java”中讲述的Reactor模式。

“Scalable IO in Java”的地址是：http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf

Reactor模式也叫反应器模式，大多数IO相关组件如Netty、Redis在使用的IO模式，为什么需要这种模式，它是如何设计来解决高性能并发的呢？



参考文档：  
https://www.cnblogs.com/crazymakercircle/p/9833847.html