当我们谈论起Java各版本变更，大部分人都会说。

JDK9支持了模块化，引入了ZGC。

JDK10支持了自定义变量var。

JDK11是JDK8后的一个长期支持版本，支持了JFR。

···

JDK14删除了CMS垃圾回收器。

JDK15默认禁用偏向锁定，并弃用所有相关的命令行选项。

JDK16支持了Records，优化了向量计算。

JDK17是JDK11后的长期支持版本，SpringBoot 3的最低依赖版本是JDK17 ，移除了实验性的AOT、JIT编译器。

··· ···

当然由于我们的传统，目前大家使用的都还是JDK8，所以聊上面这些JDK新特性有些空中楼阁，今天这篇文章想要讨论的是在JDK8的各个小版本中，有哪些特性，会对应用程序产生影响，甚至引发bug？以及对于JDK8的各个小版本的选择是越高越好吗？



这里我们先整个活，统计一下容器平台里开发测试环境里的JDK版本使用情况。

目前官方推荐的基础镜像中的JDK版本的jdk1.8.0_312。统计了目前容器平台的22465个容器，在其中的14886个Java容器中，使用量Top3的三个JDK版本分别是jdk1.8.0_292、jdk1.8.0_272、jdk1.8.0_242三个版本，数目分别是7284、4589、1292.

剩下的还有三位数的jdk1.8.0_181、jdk1.8.0_252、jdk1.8.0_232、jdk1.8.0_151几个版本，以及一些零星的项目组会使用JDK1.6、JDK1.7甚至oracle jdk的版本。最后目前官方基础镜像中jdk1.8.0_312只有42个容器使用了该版本JDK。

顺便在我们往下聊之前，建议你看一下你的本机开发的JDK小版本、开发测试的JDK小版本、生产环境的JDK小版本是否一致。



## 起 一桩由JDK8小版本引发的bug

关于这个问题的起源，是来源于SCC开发的过程中使用的Kubernetes Java Client客户端，类似我们有操作MySQL、Redis、Kafka、OpenStack的客户端，GIthub上也有操作Kubernetes集群的开源客户端。然而在去年7、8月，SCC使用该客户端的时候却发生了一些问题，Kubernetes集群的信息在同步过程中经常同步不上来，当然事后我们也花了一个月左右找到几个问题项进行了修复。其中一个问题就和JDK有关。该bug的地址在 ：https://github.com/fabric8io/kubernetes-client/issues/2212 。问题描述其实很诡异：当把系统的JDK版本从8u242升级到8u252时，应用无法正常使用了，开始有如下报错。而如果回滚到之前的JDK版本则应用可以正常运行。

```text
Caused by: java.net.SocketException: Broken pipe (Write failed)
	at java.net.SocketOutputStream.socketWrite0(Native Method)
	at java.net.SocketOutputStream.socketWrite(SocketOutputStream.java:111)
	at java.net.SocketOutputStream.write(SocketOutputStream.java:155)
	at sun.security.ssl.OutputRecord.writeBuffer(OutputRecord.java:431)
	at sun.security.ssl.OutputRecord.write(OutputRecord.java:417)
	at sun.security.ssl.SSLSocketImpl.writeRecordInternal(SSLSocketImpl.java:894)
	at sun.security.ssl.SSLSocketImpl.writeRecord(SSLSocketImpl.java:865)
	at sun.security.ssl.AppOutputStream.write(AppOutputStream.java:123)
	at org.apache.flink.kubernetes.shaded.okio.Okio$1.write(Okio.java:79)
	at org.apache.flink.kubernetes.shaded.okio.AsyncTimeout$1.write(AsyncTimeout.java:180)
	at org.apache.flink.kubernetes.shaded.okio.RealBufferedSink.flush(RealBufferedSink.java:224)
	at org.apache.flink.kubernetes.shaded.okhttp3.internal.http2.Http2Writer.settings(Http2Writer.java:203)
```



根据issue下的讨论，经过很多人在不同环境下的实验，最终有位老哥debug到问题的原因在于在JDK8的不同版本下，okHttp返回的协议protocol不同，导致请求建链执行的逻辑有了差异。而之所以JDK8u252之前和之后的protocol有不同，是因为JDK8u252之后的JDK8版本在okhttp中被错误的认为成了Jdk9Platform ，而此时okHttp会启用http2的okhttp，而不是应该使用的http/1.1 协议，从而触发bug。

![image-20220207134717409](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207134717409.png)



虽然现象看上去是因为JDK版本的问题，因为只要回滚JDK版本问题就会消失。但是结论一般认为是okHttp的代码有问题，把JDK8u251之后版本认为是JDK9（这段是因为OpenJDK 8u252 包含从 Java 9 向后移植的 ALPN API。okhttp中使用的Jetty，Jetty ALPN 代理版本 2.0.10如果检测到 OpenJDK 版本为 8u252 或更高版本，则**不会执行类重定义。**参考文档：https://webtide.com/jetty-alpn-java-8u252/），从而使用Http2协议建链，导致了应用逻辑失败。



Kubenretes Client之后在该问题中进行了修复，在判断Java版本是JDK8时，配置okhttp的DisableHttp2属性，关闭掉Http2。或者直接在操作系统层配置export HTTP2_DISABLE=true都可以解决该问题。之后okHttp也修复了该bug，见下图。

![image-20220207141924190](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207141924190.png)



问题到这解决了，但是做为一个开发者，其实我们发现整个问题是因为JDK升级了，但是我们使用的依赖包却因为JDK产生了问题，而排查过程其实很困难，毕竟业务层面日志基本不会有异常，但是实际确实是对业务产生了影响。那么是不是随着JDK升级还会有其他类似的问题产生呢？JDK自身升级一般是为了引入新功能和解决bug，那它会不会引入新的bug？哪怕JDK没有bug，我们引入的依赖包会不会因为JDK版本变化产生bug呢？



## 承 以一般理性而言，高版本会修复更多的bug

首先关于JDK8的小版本的各个版本changelog，我们可以在java官网找到相关链接。https://www.java.com/zh-CN/download/help/release_changes.html 其中每个小版本基本都由新功能、删除的功能、bug修复、其他说明组成。

![image-20220207154035462](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207154035462.png)

注意这里的说明只是当前版本发布时已知而未被修复的问题，实际使用中用户可能会发现更多问题，这些会在JBS（Java Bug System）中得到记录 https://bugs.openjdk.java.net/。



这部分我们会聊一聊Docker中容器识别CPU、内存的有关bug和修复。这是一个很经典的例子。

对于JDK自身以及很多Java应用来说，需要拿到OS级别的CPU、内存容量进行相关配置。比如JVM默认会使用宿主机内存大小的1/64做为启动堆内存，宿主机内存的1/4做为堆内存最大值。JDK8默认的ParallelGC，默认使用公式“ParallelGCThreads = (ncpus <= 8) ? ncpus : 3 + ((ncpus * 5) / 8)” 来计算做并行GC的线程数。而在JDK8的较低版本中（8u131），容器中的程序会将这里的内存、CPU值的识别成宿主机的CPU、内存。对于一个2C4G的容器，它如果拿到宿主机64C 512G的参数，就会导致容器OOM或者并发GC线程过多的CPU占用过高。下面来看下这个bug的修复。

https://bugs.openjdk.java.net/browse/JDK-8170888

https://bugs.openjdk.java.net/browse/JDK-6515172



![image-20220207171322605](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207171322605.png)





在Java 8u131和Java 9之前，JVM是不能识别容器设置的内存或cpu限制的。Java9已经有了一些实验性功能参数，依然有些缺陷，在Java 10中，内存限制会自动识别容器的CGroup限制并强制执行，并且之后该功能已经反向移植到Java-8u191。通过上图可以看到，该bug已经反向修复到JDK8u191。其实修改的逻辑也很简单，因为容器所使用的参数已经在宿主机cgroup文件中声明了，只要读取对于cgroup文件即可。所以实际的代码修改如下：

![image-20220207172048074](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207172048074.png)



对于限制容器中应用看到的CPU、内存，Java9中引入了 -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap 两个参数，这两个参数是顺序敏感的，可以限制JVM使用的内存。而对于 CPU 核心数限定，Java 已经被修正为可以正确理解“–cpuset-cpus”等设置，无需单独设置参数。只要Kubernetes中的Yaml文件中声明了对应Limit即可。



因此可以认为在JDK 8u191之后的版本就可以正确读取到容器里的Limit值，而不会拿到宿主机的相关参数了。



事实上这些能在jdk8中被修复的bug算是好的了，还有一些bug到目前为止并未在JDK8中修复，这里只是简单介绍下，不做展开：

“FutureTask.isDone returns true when task has not yet completed” https://bugs.openjdk.java.net/browse/JDK-8073704 

P4 bug，影响到8u20，9，在JDK9中被修复。netty 4版本受到其影响 [method `io.netty.util.concurrent.DefaultPromise#cancel/isDone` violates contract? · Issue #7712 · netty/netty · GitHub](https://github.com/netty/netty/issues/7712)  

https://github.com/netty/netty/issues/7712

![image-20220207161726222](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207161726222.png)



“Repeated offer and remove on ConcurrentLinkedQueue lead to an OutOfMemoryError”  https://bugs.openjdk.java.net/browse/JDK-8054446

P3 bug，影响JDK 7，8，9.于JDK9中修复，错误使用ConcurrentLinkedQueue的remove操作可能会导致内存泄漏导致OOM。Jetty曾经就踩了这个bug。

![image-20220207161618571](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207161618571.png)





## 转 但是仍有高版本功能引入的bug

确实一般高版本的出现会解决更多的bug，但是我们是不是直接无脑上高版本就完事了？如果答案真的如此就简单了。
JDK做为一个程序也是不断演进的，而高版本除了修复bug，也会合并一些高版本（JDK9及以上）的功能进来。而新功能就意味着也可能带来了新的bug。来看下面的一个bug https://bugs.openjdk.java.net/browse/JDK-8227006



![image-20220207161901406](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207161901406.png)



简单的来说该bug就是说，自JDK8u191以来，在 linux 环境下，Runtime.availableProcessors 执行时间增加了 100 倍。

也就是说在JDK8u191的程序没有这个问题，而JDK8u191以后的程序就会遇到这个问题。

就影响范围而言，Runtime.availableProcessors 该命令会影响Java并发工具CompletableFuture.waitingGet 的性能，而CompletableFuture又是各中间件中广泛使用的异步框架。因此从而在某些情况下会导致应用的性能急速下降。

随着问题的定位，最后代码落到JVM CPP代码 src/hotspot/os/linux/osContainer_linux.cpp 中的“OSContainer::is_containerized()” 方法。是的，如果你还记得上一小节的内容，你应该还记得JDK8u191进行了一次JDK9的功能合并，解决了JVM的容器系统识别。而该bug就是该功能引入导致的。

![image-20220207164355871](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207164355871.png)

随着这个功能的合入，jdk1.8.0_191以后的Java程序可以正确识别容器中的limit值，使用该值进行相关计算。但因为这个操作，导致了OS该调用的效率比之前下降了100倍。如果你的系统或者中间件在某个时间段有大量Runtime.availableProcessors 系统调用，可能就会遇到该bug。

最后很多大佬基于这个问题给出了很多解决方案，并针对各种解决方案进行讨论，最后还是选择了实现起来比较简单的 cache 方案，虽然这个方案也有一点瑕疵，但是出现的概率非常低且是可以接受的。

![image-20220207164954654](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207164954654.png)





## 合  给出结论



根据我们上面的讨论，JDK8的小版本每次升级一般可以修复了数十的bug，同时也会合并了一些高版本的功能进来。但是毕竟是程序就一定会有bug，随着新功能的引入可能也会有新的bug出现。无论是JDK本身或者各个开源组件都有可能踩到，对于应用来说这些坑可能大概率不会直接踩到，但是一旦踩到想去找到可能比解决业务导致的bug麻烦得多。当然长远来看，引入新功能，导致新bug，解决新bug，其实是一个螺旋上升的过程，bug最终会被修复，只是看这个bug存在的期间会不会影响到你。



做为一个日常开摆的人，个人观点：若非强功能(包括安全)需要，可以不升级，但是组里最好有人能cover具体使用。

以及千万不要出现开发测试使用一个JDK版本、生产环境使用一个JDK版本的问题了。



PS:最后再聊聊有关JDK大版本升级的话题，JDK大版本升级感觉已经是Java社区老生常谈的一个问题了。虽然现在SpringBoot3强势站队JDK17，但是很多开源组件并没有及时更上，参考java官网的质量推广计划 https://wiki.openjdk.java.net/display/quality/Quality+Outreach 上面记录了各开源组件对JDK各版本的适配，比如WorksFineOnJDK9、WorksLikeHeavenOnJDK11、AllTestsGreenOnJDK14，对于17的支持就更少了。目前看来还是有很多项目不支持的高版本JDK的，比如就有Druid就有因为不支持高版本JDK而导致用户迁移到Hikari。

![image-20220207144034017](C:\Users\汪劲松\AppData\Roaming\Typora\typora-user-images\image-20220207144034017.png)



目前来看JDK8在较长一段时间应该还是主流，但是5到10年后，高版本的JDK一定会逐渐成为主流。顺便Java社区给人的印象并没有CNCF社区那样开放活跃，可能也有JDK8太长久的原因。



End.



