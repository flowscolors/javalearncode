
## 1.Eureka介绍

Eureka的服务提供者往EurekaServer的注册中心注册服务，消费者从注册中心定期拉取服务列表。  

Eureka Server Cluster 多服务节点之间使用P2P的同步复制交换服务注册信息。


## 2.Eureka可以实现的功能
1. 把服务注册到EurekaServer，消费者端直接在项目中添加EurekaClient依赖，就可以通过服务名称来获取信息了。
  不引入Fegin、Ribbon也可以实现调用，从DiscoveryClient类中获得服务名称的服务实例URL，自己拼接后去调用。  消费端直接调用客户端。  
  
2. 引入Fegin、Ribbon。为了解决上一步方法调用的自己拼接，引入Fegin、Ribbon，消费端就可以通过服务名来调用服务，并可以负载均衡。
   Ribbon 需要自己创建RestTemplate，在这个Http客户端上使用@LoadBalanced 注解，于是就可以解析并按服务名进行调用。  

参考文档：  
http://antsnote.club/2018/09/05/SpringCloud-Eureka%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E4%B8%8E%E5%8F%91%E7%8E%B0/


## 3.Eureka二次开发添加的功能
1.默认Eureka会展示所有注册中心实例，没有对用户权限控制。需要根据不同用户权限在Admin进行开发。
所有的注册信息可以从 EurekaServerContextHolder.getInstances().getServerContext().getRegistry().getApplication()中获得，按登录用户名返回对应数据即可。

2.需要对Eureka的HTTP请求进行拦截校验，防止别人使用接口关掉了某个重要服务。

3.拓展实例信息。Eureka Server端保存注册实例信息的数据结构是一个双层VoncurrentHashMap。第一次key为applicationName，value是Map
    第二层key是InstanceId，value是Lease对象。

## 4.Eureka API接口
Eureka提供了API来供非Java程序来使用Eureka进行相关操作。

包括注册应用实例、注销应用实例、查询所有实例、发送实例心跳等等。

接口文档：
https://github.com/Netflix/eureka/wiki/Eureka-REST-operations
https://blog.csdn.net/fu_huo_1993/article/details/115255222



