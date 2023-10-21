
在实际开发中，有太多故障因为没有设置超时时间导致服务“hang”住，或者OOM异常。又或是没有设置keepalive，导致长连接异常中断。

## 超时场景
* 代理层超时和重试
Haproxy、Nginx、Twemproxy等代理组件，都会设置和后端的超时时间。

* Web容器超时
Tomcat、Jetty等提供Http服务器的容器环境，需要设置客户端和服务器的超时时间。

* 中间件客户端的超时和重试
各消息中间件的客户端、Kafka Client、Kubernetes Java Client、HttpClient、CXF 都会要配置网络连接和超时时间。

* 数据库客户端的超时
MySQLClient需要配置JDBC Connection、Statement的超时时间，这里需要注意如果使用Druid等连接池工具，也需要配置超时时间。

* 业务超时
超时订单取消任务、超时时间关闭，可以通过Future来给业务代码限制超时时间

* 前端超时时间
前端比如Vue等框架中的Ajax访问后端接口可以限制超时时间。

* Linux服务器的超时时间
