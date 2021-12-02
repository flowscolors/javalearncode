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
 
 相关报错：
 "failed invoking null event handler: {}"