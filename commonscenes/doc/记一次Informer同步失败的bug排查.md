## 问题现象
1.本地开发、开发测试环境正常。上到生产环境，纳管集群数目增多，发现有部分同步异常，体现为watch不到消息。


## 排查思路
首先根据java go的逻辑，把问题二分成两部分。Java收到了，但是处理中丢了。或者Java根本没收到，apiserver根本没发。或者Apiserver
发了但是Java没收到，网络丢包。

1.Java程序通过打断点，找堆栈，还原整个watch事件在client的处理逻辑。并在各个处理逻辑处打出日志。
2.Apiserver通过调整日志等级到--v=10，还原部分信息。

Java部分:
可能1.Reflactor没收到信息
    最后确实在某种情况下，watch事件没有走到Reflactor部分。当然一开始并没有查到这里。
   
可能2.SharePorcess事件没有被出队消费
    正常情况事件最后会被放到阻塞队列中，然后依次出队消费。
   
可能3.watch组件没有收到消息
    这也是最后查明的原因，最后重写了okhttp的部分源码，增加日志和调用明确了这个问题。

Go部分：
可能1.Java和Apiserver并没有建链
    通过netstat命令，已经对v10等级的apiserver日志排查，明确确实一直有长连接的链在。

可能2.Apiserver建链了但是并没发送信息。
    这点其实一开始就可以排除，毕竟scheduler、controller manager也是watch这个apiserver。
    但是在v10日志中，有关于的报错，并且我们并没有修改默认的apiserver default-watch-cache-size参数。
   
    “kubernetes fast watcher slow processing. number of buffered events”
    原因和 https://github.com/kubernetes/kubernetes/issues/33653有关  
    参考文档 https://github.com/kubernetes/community/blob/master/contributors/design-proposals/api-machinery/apiserver-watch.md
    watch-cache-size  https://github.com/kubernetes/kubernetes/issues/57105
    
 可能3.ApiServer发送信息了，但是没有传到Java程序  
    可以通过Java程序websocket端获得信息。
 
 ## 3.问题解决
 1.在websocket的on message方法中添加日志，发现对应15个资源对象，每个集群中只有若干会被watch到。其他事件一直没有收到，定位问题在于watch。  
 并更加使用经验得到多个client可以规避该情况的方法。
 
 2.定位OKHttp源码导致OkHttpClient的connectionPool连接池中连接复用，后面的连接可以使用前面连接的端口进行调用请求，从而覆盖watch操作。  
 
 3.修改informer初始化方法即可解决问题。
 
 
 ## 4.查找步骤
 当然大部分还是okhttp client的代码，注意这里的两个关键部分 线程池和连接池。
 1.io.fabrica8.kubernetes.client.dsl.base.BaseOperation 中的watch方法，要求传入ListOptions options，Watcher watcher。会根据client、baseOperation、options、watcherToggle创建一个watcher对象。
 而实际BaseOperation就已经定义好了是对哪种资源对象做操作 type、以及查询的label、index等。并且BaseOperation继承了OperationSupport类，这个类中有一个final的OkHttpClient，并且规定了resource、namespace。以及提供了getRootUrl()、getNamespacedUrl()方法设置实际Request中的url。
 
 io.fabrica8.kubernetes.client.dsl.internal.WebSocketClientRunner 中的run方法，会执行建立webSocket连接。被      WatchManager.runWatch()方法调用。
 ```text
 @Override
 public void run(Request request){
     client().newWebSocket(request,newLister(queue,webSocketRef));
 }
 ```
 作为WebSocketClientRunner启动的类，使用了很多保证并发的类，比如queue就是一个长度为1 ArrayBlockingQueue(1)，webSocketRef是一个AtomicReference<WebSocket>。但是创建时候都是null。
 
 2.vmtool --action getInstacnces --ClassName okhttp3.WebSocket -x 2
 得到两个RealWebSocket 分别对应Pod Event接口，两者listener、random、key、read、write、executor都不一样
 
 检查RealWebSocket，检查建链的接口 vmtool --action getInstacnces --ClassName okhttp3.internal.ws.RealWebSocket -x 2 --limit -1 | grep originalRequest
 可能同一个资源会有相同URL但是resourceVersion不同的RealWebSocket··· 也有同样resourceVersion的出现。
 
 
 3.vmtool --action getInstacnces --ClassName okhttp3.ConnectionPool -x 2
 得到ConnectionPool中的maxIdleConnections为5。主要就是保证连接池中的连接一直wait。
 vmtool得到内含1251个连接池ConnectionPool，并且全局使用同一个ThreadPoolExecutor,因为是final [Running,pool size = 1027,active threads = 1027,queued tasak = 0.completed tasks = 4956]
 得到1051个RealWebSocket，每个RealWebSocket对应真正的apiServer URL。    
 
 4.vmtool --action getInstacnces --ClassName okhttp3.internal.connection.RealConnection -x 2
 得到三个RealConnection，都在connectionPool中，每个连接的rawSocket、socket的URL相同，因为都是只到6443，只是localPort端口不同，
 successCount 两个是2，一个是0。需要考虑allocations、allocationLimit两个变量。
 在RealConnection中有isEligilbe()方法，三个判断步骤，都满足则返回true。连接可以被复用，已经满足。
 
 RealConnection是socket物理连接的包装，它里面维护了List<Reference<StreamAllocation>>的引用。List中StreamAllocation的数量也就是socket被引用的计数，如果计数为0的话，说明此连接没有被使用就是空闲的，需要被回收；如果计数不为0，则表示上层代码仍然引用，就不需要关闭连接。
 
 
 参考文档：
 https://cloud.tencent.com/developer/article/1199053
 https://blog.csdn.net/sinat_36553913/article/details/104054160
 http://quibbler.cn/?thread-638.htm
 https://blog.csdn.net/sinat_36553913/article/details/104054028

 
 相关报错：
 "failed invoking null event handler: {}"