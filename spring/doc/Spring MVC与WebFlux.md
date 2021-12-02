
webmvc和webflux作为spring framework的两个重要模块，代表了两个IO模型，阻塞式和非阻塞式的。

webmvc是基于servlet的阻塞式模型（一般称为oio），一个请求到达服务器后会单独分配一个线程去处理请求，如果请求包含IO操作，线程在IO操作结束之前一直处于阻塞等待状态，这样线程在等待IO操作结束的时间就浪费了。

webflux是基于reactor的非阻塞模型(一般称为nio)，同样，请求到达服务器后也会分配一个线程去处理请求，如果请求包含IO操作，线程在IO操作结束之前不再是处于阻塞等待状态，而是去处理其他事情，等到IO操作结束之后，再通知（得益于系统的机制）线程继续处理请求。

这样线程就有效地利用了IO操作所消耗的时间。



参考文档：  
https://www.cnblogs.com/crazymakercircle/p/14312282.html
https://www.cnblogs.com/crazymakercircle/p/11704077.html