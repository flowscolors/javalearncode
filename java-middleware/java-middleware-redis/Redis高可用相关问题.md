## 1.常用高可用模式
参考文档：
[三种模式](https://www.cnblogs.com/jian0110/p/14002555.html)
[Redis面试问题](https://blog.csdn.net/ThinkWon/article/details/103522351)
### 1.1哨兵模式


### 1.2 主从模式

### 1.3 集群模式
集群模式的主从
怎么判断某个节点挂了？  每个节点存了集群所有主从节点信息，一半以上节点ping不通认为下线，去连备节点。

怎么判断整个集群挂了？ 三种，某个主和该主所有从都挂了；超过半数以上主挂了；任意主挂了，且没有从。

Redis的投票机制？

* Redis集群不保证数据一致性，特定情况下，redis集群会丢失已执行的写命令
* 异步复制可能会导致丢失写命令，从升主，那这段时间的写命令丢失。

[集群主从的一些原理](https://www.cnblogs.com/dadonggg/p/8628735.html)




---
## 2.AOF 与 BDF




## 3.实际使用的问题
### 3.1 RedisCluster集群模式下master宕机主从切换期间Lettuce连接Redis无法使用报错Redis command timed out的问题
场景描述：主从切换，从升为主，大概需要15s。jedis的15s主从切换后恢复服务，lettuce持续无法恢复。 
问题原因：Lettuce需要刷新节点拓扑视图，可以redis主动刷、或者客户端刷新。
问题解决: 调整刷新配置。

[Redis客户端主从切换连接异常](https://blog.csdn.net/ankeway/article/details/100136675)