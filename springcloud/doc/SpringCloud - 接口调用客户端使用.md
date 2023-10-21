
接口调用客户端有很多种，常见的Rest-Template、HttpClinet，而SpringCloud的Feign、Dubbo都可以这么算。

## Feign使用
特点：
1.基于声明式调用，而非命令式。从而不必每次使用都写明URL，从而Feign的调用就像使用本地方法调用完成服务的请求。

2.Feign简化了请求的编写，但是可以动态选择HTTP客户端实现，可以结合Eureka、Ribbon、Hystrix实现服务发现、负载均衡、熔断。

3.支持注解进行消息转换、HTTP解码。



## Dubbo与SpringCloud集成
新建项目可以直接使用Dubbo，对于SpringCloud的集成使用就需要一些改造。Java的底层实现基于Netty。

* 1.将Dubbo服务的对外接口暴露为Rest API结论
对于Dubbo服务提供者来说，可以tongg@RestController来封装服务端代码，对外暴露Rest API。
对于Dobbo服务消费者来说，在原有的@FeignClient的消费者，对应方法上加上@RequestMapping注解即可。

* 2.将SpringCloud服务Dubbo化
替换原有SpringCloud的Feign的底层调用协议，将原本底层的HttpClient调用替换成Dubbo RPC来处理，此时原本对外提供的REST API全部会成为Dubbo服务。
参见Github：https://github.com/apache/dubbo-spring-boot-project


## gRPC与SpringCloud集成
gRPC的特点之一就是跨语言，它要求你先通过IDL文件定义服务接口的参数和返回值类型，然后通过代码生成程序服务端和客户端的具体实现代码。
通信协议采用HTTP2，IDL采用了ProtoBuf（接口定义语言），类似SOAP的WASL。

Java客户端的底层实现基于Netty。
参见：https://github.com/yidongnan/grpc-spring-boot-starter