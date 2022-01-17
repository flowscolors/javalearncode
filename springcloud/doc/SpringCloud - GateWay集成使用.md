
## 网关
微服务网关在微服务架构中作为HTTP请求的统一入口，用来屏蔽和隔离内部服务实现的细节，保护、增强和控制对微服务的访问。

微服务网关作为微服务统一的入口，可以统筹和管理后端服务，主要分为数据平面和控制平面。

* 数据平面。真正执行路由转发请求的模块，以SpringCloud Gateway为例，可通过filter实现协议转换、安全认证、熔断限流、灰度发布、日志管理、流量监控等。

* 控制平台。上述功能配置的下发，对微服务API的控制、添加标签，进行编排，配置Swagger文档整合所有API的文档。

* 路由功能。网关核心功能，可单独使用或搭配注册中心使用。

* 负载均衡。网关发往后端请求可以进行负载均衡。

* 协议准换。构建异构系统核心，网关作为单一入口，整个转换后端REST、gRPC、AMQP、Dubbo等不同协议，面向Web Mobile、开放平台提供统一能力。

* 安全认证。用户在网关做身份认证，从而避免所有微服务的单独开发。

* 黑白名单。

* 灰度发布。根据请求的HTTP标记发往对应后端。

* 流量染色。根据HTTP请求的Host、Head、Agent等表示进行染色，比如添加调用链路追踪。

* 限流熔断。

* 服务管理。统计调用次数、延迟、是否熔断。

* 文档中心。整合后端所有API规范。

* 日志审计。对URL的日志请求、响应信息进行拦截。
   
## 常用网关

Nginx + Lua


Kong


SpringCloud Zuul


SpringCloud GateWay


## SpringCloud Gateway介绍
特点：非阻塞式、函数式编程。
还有一点就是Gateway是基于WebFlux的，传统的Web框架struts2，springmvc等都是基于Servlet API与Servlet容器基础之上运行的。  

Spring Cloud Gateway 的目标，不仅提供统一的路由方式，并且基于 Filter 链的方式提供了网关基本的功能，例如：安全，监控/指标，和限流。


### SpringCloud核心概念
Route：
即一套路由规则，是集URI、predicate、filter等属性的一个元数据类。

Predicate：
这是Java8函数式编程的一个方法，这里可以看做是满足什么条件的时候，route规则进行生效。

Filter：
filter可以认为是Spring Cloud Gateway最核心的模块，熔断、安全、逻辑执行、网络调用都是filter来完成的。
其中又细分为gateway filter和global filter，区别在于是具体一个route规则生效还是所有route规则都生效。

对于下面的例子，可以实现将/get的请求内部转发到/paramTest。
```shell script
@RequestMapping("/paramTest")
  public Object paramTest(@RequestParam Map<String,Object> param) {
      return param.get("name");
  }

@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
return builder.routes()
      .route("path_route", r ->
                   r.path("/get")
                  .filters(f -> f.addRequestParameter("name", "value"))
                  .uri("forward:///paramTest"))
      .build();
  }
```

* route方法代表的就是一个路由规则；
* path方法代表的就是一个predicate，背后的现实是PathRoutePredicateFactory，在这段代码的含义即当路径包含/get的时候，当前规则生效。
* filters方法的意思即给当前路由规则添加一个增加请求参数的filter，每次请求都对参数里添加 name:value 的键值对；
* uri 方法的含义即最终路由到哪里去，这里的forward前缀会将请求交给spring mvc的DispatcherHandler进行路由，进行本机的逻辑调用，除了forward以外还可以使用http、https前缀进行http调用，lb前缀可以在配置注册中心后进行rpc调用。

参考文档：
https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/
https://developer.aliyun.com/article/804348

### SpringCloud可以实现的功能
SpringCloud官方，对SpringCloud Gateway 特征介绍如下：

（1）提供基本的网关调用功能

（2）集成 Hystrix 断路器

（3）集成 Spring Cloud DiscoveryClient

（4）Predicates 和 Filters 作用于特定路由，易于编写的 Predicates 和 Filters，基于基础URL、代码、配置中心的路由配置

（5）具备一些网关的高级功能：动态路由、限流、路径重写



参考文档：  
https://www.cnblogs.com/crazymakercircle/p/11704077.html

### SpringCould 可以添加的功能
可自定义的功能：
* Spring-Cloud-Gateway 基于过滤器实现，同 zuul 类似，有pre和post两种方式的 filter,分别处理前置逻辑和后置逻辑。客户端的请求先经过pre类型的 filter，然后将请求转发到具体的业务服务，收到业务服务的响应之后，再经过post类型的 filter 处理，最后返回响应到客户端。
  过滤器执行流程如下，order 越大，优先级越低。

* 基于自定义规则来实现灰度发布、蓝绿发布。比如对于某些IP或者带某些header头的请求就固定发到一个后端。
  还有一种比较神奇的操作，对于APP，用户客户端进行开屏的时候进行登录，基于用户信息返回一个后端地址，这样就能解耦逻辑且保证具体到某个用户访问某个后端。

* 基于过滤器实现token和限流。可定义全局、局部过滤器。
1) 如果只是使用token来限制接口调用，后端+filter做就可以了。每个后端发token的时候，存当前时间和使用时间，每次用token的时候比较前面两个值和当前时间。

2）如果需要限流，则需要加一个Redis，每个后端往Redis减数据。然后数据每小时清空。

* 基于Yaml、数据库、Nacos的配置管理。
  基于Yaml只能实现静态配置，Nacos可以实现整个GateWay的配置动态配置，但是如果某个路由配置文件格式错误，热加载进去后会导致整个网关服务不可用。基于数据库实现，可基于Redis+MySQL实现，存的时候校验即可。
  
  ![Nacos动态配置](https://www.cnblogs.com/jian0110/p/12862569.html)  ！[数据库动态配置](http://www.eknown.cn/index.php/spring-boot/spring-cloud-gateway-dynamic-routes.html) ! [数据库配置2](https://www.haoyizebo.com/posts/1962f450/)

* 基于Sentinel完成流控和降级
  当触发到限流阈值时，进行相应流控和降价。返回对应的提示页、提示信息。
  
* 基于Swagger完成微服务系统API文档，实际接口URL信息可以从配置来，但是参数是要从Eureka来的。
  GateWay有所有接口信息，参数信息，使用Swagger进行封装展示。

* 基于Eureka自动同步注册中心服务列表。cloud.gateway.discovery.locator.enabled: true ,在入口类添加对应注解，开启服务注册。  
  如果没有注册中心的服务自动同步，那网关就是一个能力多点的Nginx，而正因为能自动同步微服务的注册列表，才能方便客户端调用。调用者也从网关调用，完整整个整合。  ~[GateWay整合Eureka转发服务请求](https://cloud.tencent.com/developer/article/1422239)


### SpringCloud 网关接口

GET /actuator/gateway/globalfilters

GET /actuator/gateway/routefilters

POST /actuator/gateway/refresh

GET /actuator/gateway/routes

参考文档： https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#actuator-api


### SpringCLoud 遇到的问题
1.filter都是异步调用，不像controller可以直接查什么哪个接口耗时.需要查看请求的执行时间也要用filter。
解决方案：
1）自定义GlobalFilter，当请求进入时记录开始时间，当请求结束时，减去开始时间即为具体的执行时长
2）使用特定APM工具，SkyWalking高版本就支持异步，支持SpringCloud GateWay。

参考文档：
https://blog.csdn.net/zhaokejin521/article/details/120439712

2.调用网关偶尔返回500，"Connection prematurely closed BEFORE response"。后端并未收到请求。
产生原因：
gateway调用后台服务，底层netty会使用httpclient连接池里面的连接，httpclient连接池的连接有个参数：max-idle-time，大意指的是多长时间连接不使用就关闭。如果设置为null, 连接不会关闭。

后台服务也有相应的连接对应连接池的连接，参数keepAliveTimeout，大意指后台服务的连接空闲多长时间就会自动关闭，缺省的值就是connection-timeout参数的值。如果为-1就不会有时间限制，缺省值为60s ,但是一般的再server.xml里面设置为20s.

简单来说网关的超时时间默认为null，后端服务超时时间20s，正好这个请求到就是连接断了。配置完参数即可。

参考文档：
https://jishuin.proginn.com/p/763bfbd4f0d4
https://github.com/spring-cloud/spring-cloud-gateway/issues/1148
