
## 1.Eureka介绍


Eureka的服务提供者往EurekaServer的注册中心注册服务，消费者从注册中心定期拉取服务列表。  



## 2.Eureka可以实现的功能
1. 把服务注册到EurekaServer，消费者端直接在项目中添加EurekaClient依赖，就可以通过服务名称来获取信息了。
  不引入Fegin、Ribbon也可以实现调用，从DiscoveryClient类中获得服务名称的服务实例URL，自己拼接后去调用。  消费端直接调用客户端。  
  
2. 引入Fegin、Ribbon。为了解决上一步方法调用的自己拼接，引入Fegin、Ribbon，消费端就可以通过服务名来调用服务，并可以负载均衡。
   Ribbon 需要自己创建RestTemplate，在这个Http客户端上使用@LoadBalanced 注解，于是就可以解析并按服务名进行调用。  
   


## 3.Eureka可以添加的功能

参考文档：  
http://antsnote.club/2018/09/05/SpringCloud-Eureka%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E4%B8%8E%E5%8F%91%E7%8E%B0/