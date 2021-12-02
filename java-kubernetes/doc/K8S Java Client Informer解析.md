Informer概要设计
![](https://gitee.com/daniel-hutao/images/raw/master/client-go.png)

如上图所示，我们在编写自定义控制器的过程中大致依赖于如下组件，其中浅黄色的是自定义控制器里需要编码的部分，浅蓝色的是 client-go 提供的一些“工具”。
当然实际使用中你也可以选择不使用informer，自定义list方法+直接使用watch，这个可以让自己可以控制的部分更多一些。  

Reflector：Reflector 向 apiserver watch 特定类型的资源，拿到变更通知后将其丢到 DeltaFIFO 队列中；
Informer： Informer 从 DeltaFIFO 中 pop 相应对象，然后通过 Indexer 将对象和索引丢到本地 cache 中，再触发相应的事件处理函数（Resource Event Handlers）运行；
Indexer： Indexer 主要提供一个对象根据一定条件的检索能力，典型的实现是通过 namespace/name 来构造 key ，通过 Thread Safe Store 来存储对象；
Workqueue：Workqueue 一般使用的是延时队列实现，在 Resource Event Handlers 中会完成将对象的 key 放入 workqueue 的过程，然后我们在自己的逻辑代码里从 workqueue 中消费这些 key；
ClientSet：Clientset 提供的是资源的 CURD 能力，和 apiserver 交互；
Resource Event Handlers：我们在 Resource Event Handlers 中一般是添加一些简单的过滤功能，判断哪些对象需要加到 workqueue 中进一步处理；对于需要加到 workqueue 中的对象，就提取其 key，然后入队；
Worker：Worker 指的是我们自己的业务代码处理过程，在这里可以直接接收到 workqueue 里的任务，可以通过 Indexer 从本地缓存检索对象，通过 Clientset 实现对象的增删改查逻辑。

## Java对应组件、方法实现

Cache           [Cache](https://github.com/fabric8io/kubernetes-client/blob/v5.8.0/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/informers/cache/Cache.java )  
ShareProcessor  [ShareProcessor](https://github.com/fabric8io/kubernetes-client/blob/v5.1.1/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/informers/cache/SharedProcessor.java )    
ListerWatcher   [ListerWatcher](https://github.com/fabric8io/kubernetes-client/blob/v5.8.0/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/informers/ListerWatcher.java)  
ProcessorListener  [ProcessorListener](https://github.com/fabric8io/kubernetes-client/blob/v5.8.0/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/informers/cache/ProcessorListener.java)

HttpClientUtil  [HttpClientUtil](https://github.com/fabric8io/kubernetes-client/blob/v5.8.0/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/utils/HttpClientUtils.java)



### 相关bug与改进
实际上不同语言的实现导致虽然上层看起来的一致的，但是底层其实并不相同。并且可能每个版本各自也有不同。
Java client就在不断演化的过程中：
通过 Informers 简化/整合线程使用【已实现】  https://github.com/fabric8io/kubernetes-client/issues/3072


## Go对应组件、方法实现

chunked机制
Reflector  
ListWatcher
Indexer
ThreadSafeStore  
DeltaFIFO  
Workqueue
Informer

参考文档:  
https://www.danielhu.cn/post/k8s/client-go-summary/  
https://www.danielhu.cn/post/k8s/client-go-informer/
https://www.cnblogs.com/luozhiyun/p/13833160.html
