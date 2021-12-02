
作为十分常用的底层基础包，OKhttp被使用在java的很多场景中，包括业务的调用、android底层的使用。

## OKHttp 组件概要


maxRequests和maxReuestsPerHost值的设置与executorService线程池的设置有关联，请注意。maxRequests和maxRequestPerHost是okhttp内部维持的请求队列，而executorservice是实际发送请求的线程。如果maxRequests和maxReuestPerHost设置太大，executorService会因为线程太少而阻塞发送。

## OkHttp源码解析

连接池ConnectionPool
    连接池中连接复用机制。 https://blog.csdn.net/chunqiuwei/article/details/74203667

Search 关键字: okhttpclient connection pool 释放

参考文档:
https://mrfzh.github.io/2019/07/19/okhttp3%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90%EF%BC%88%E4%B8%89%EF%BC%89%EF%BC%9A%E5%A4%8D%E7%94%A8%E8%BF%9E%E6%8E%A5%E6%B1%A0/
https://juejin.cn/post/6844904196219600910
https://www.cnblogs.com/ganchuanpu/p/9408081.html
https://www.jianshu.com/p/a2fcf1dad6b5


## OKHttp踩坑记录

参考文档：
https://stackoverflow.com/questions/49069297/okhttpclient-connection-pool-size-dilemma
https://juejin.cn/post/6898145227765186567
https://blog.csdn.net/sinat_36553913/article/details/104054028