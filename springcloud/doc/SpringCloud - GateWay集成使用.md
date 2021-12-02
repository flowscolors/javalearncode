## 1.SpringCloud Gateway介绍
特点：非阻塞式、函数式编程。
还有一点就是Gateway是基于WebFlux的，传统的Web框架struts2，springmvc等都是基于Servlet API与Servlet容器基础之上运行的。  

Spring Cloud Gateway 的目标，不仅提供统一的路由方式，并且基于 Filter 链的方式提供了网关基本的功能，例如：安全，监控/指标，和限流。

## 2.SpringCloud可以实现的功能
SpringCloud官方，对SpringCloud Gateway 特征介绍如下：

（1）提供基本的网关调用功能

（2）集成 Hystrix 断路器

（3）集成 Spring Cloud DiscoveryClient

（4）Predicates 和 Filters 作用于特定路由，易于编写的 Predicates 和 Filters，基于基础URL、代码、配置中心的路由配置

（5）具备一些网关的高级功能：动态路由、限流、路径重写



参考文档：  
https://www.cnblogs.com/crazymakercircle/p/11704077.html

## 3.SpringCould 可以添加的功能
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


