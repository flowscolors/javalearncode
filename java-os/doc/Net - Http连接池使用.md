
Http连接池，一般基于httpclient的builder方法创建，使用IOC注入，整个项目使用一个http连接池。

一般builder需要注意的创建参数有:
registry  需要注意registry注册http、https两种请求
PoolingHttpClientConnectionManger  连接池参数
PoolingHttpClientConnectionManger.setMaxTotal 最大连接数
PoolingHttpClientConnectionManger.setDefaultMaxPerRoute 同路由并发数
retryHandler    重试次数
setKeepAliveStrategy  设置长连接配置
setConnectionTimeout  设置连接超时时长
setReadTimeout        设置读取超时时长


## HttpClient连接池
httpClientBuilder.setConnectionManger(***) 常用的几种连接池

### BasicHttpClientConnectionManger


### PoolingHttpClientConnectionManger


### MultiThreadedHttpConnectionManager
HttpClient3.1 中开始引入

参见MultiThreadedHttpConnectionManager 源码。
连接池在分配连接时调用的 doGetConnection 方法时，对能否获得连接，不仅会对参数 maxTotalConnections 进行是否超限校验，还会对 maxHostConnections 进行是否超限的校验。
 maxHostConnections 的含义:
* 每个host路由的默认最大连接,需要通过setDefaultMaxConnectionsPerHost来设置,否则默认值是2。
* 如果不设置DefaultMaxConnectionsPerHost，则会导致每个请求的Host并发连接数只有2，限制了线程获取连接的并发度(此时观察tcp并发度的时候会发现只有2个连接建立 2 ESTABLISHED)

maxHostConnection属于HttpClient线程池中的常见坑了。