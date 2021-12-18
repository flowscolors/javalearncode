
随着调用链路的增多，应用的稳定性，出现问题的快速定位原因，已经是大家越来越重视的能力了。

于是有了可观测性的三大要素：监控 Metrics、日志 Logging、链路 Tracing。三种数据在可观测性中都有各自发挥的空间，每种数据都无法完全被其他数据替代。

一般而言我们需要使用Metrics、Tracing、Logging去联合排查问题，当然根据不同场景会有不同的结合方案。一些简单的问题可能直接看到告警日志就能直接定位问题，但是一个良好的可观测性系统必须具备上述三种数据。


常见解决方案：
监控： Prometeus、Grafana
日志： ELK、Splunk、Loki
链路： SkyWalking、Jaeger、Zipkin、dynatree、epbf

而问题是实际情况中，因为基础设施在不同组人手中，并没有同时能查看三者的人。顺便以上三者只解决观测性问题，具体问题解决还需要实际开发人员解决。

因为在不同组中，如果我想统一查看，其实并不一定有一个全局唯一id去在三个平台查。


## OpenTelemetry 
三大观测领域中，总需要有一个来承担起牵头作用，OpenTelemetry选择的就是Tracing，使用trace_id、span_id来串起所有。
所以在OpenTelemetry中会把metrics和log中都添加上trace_id、span_id字段。解决方案就是定义一套标准，并提供一系列组件，让各类中间件支持其协议。

OpenTelemetry提供SDK给应用，应用引用后，Tracing、Metrics功能由SDK实现，Logging也可由SDK实现，或增强现有Log组件能力。

OpenTelemetry 的 java sdk 中，应用可以无需修改 code，只需要引用对应的 jar 包就可以完成对很多常见观测能力的集成。目前支持的集成已经比较广泛，包含常见的软件集成。主要包含，akka，camel，dubbo，httpclient，cassandra，couchbase，elasticsearch，finatra，geode，grails，grizzly，grpc，guava，get，hibernate，hystrix，jaxrs，jaxws，jdbc，jedis，jetty，jms，jsf，jsp，kafka，kubernetes，log4j，logback，mongo，netty，quartz，rabbitmq，reactor，redisson，rmi，rocketmq，scala，servlet，spark，spymemcached，struts，spring，tomcat，vertx等。使用了 OpenTelemetry 的 java 的 SDK，就可以自带这些软件的调用分析能力。



参考文档：
https://mp.weixin.qq.com/s/dFEXnCkVt7L_apAXeSlwmg

