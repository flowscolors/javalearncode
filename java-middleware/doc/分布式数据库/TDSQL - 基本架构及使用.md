
TDSQL MySQL版（TDSQL for MySQL）是部署在腾讯云上的一种支持自动水平拆分、Shared Nothing 架构的分布式数据库。TDSQL MySQL版 即业务获取的是完整的逻辑库表，而后端会将库表均匀的拆分到多个物理分片节点。

参考文档： https://cloud.tencent.com/document/product/557

## 核心特性
入口流量进入Proxy层，实际存储多Set区域、Set下多Server中Agent+MySQL，做主从。
SQL语句通过负载均衡进入到SQL引擎层，SQL引擎再根据ZK中的路由信息把对应的SQL语句发送到指定的Set节点。
绑定在Set节点的Agent服务负责监控Agent的存活状态，当发现主节点MySQL存活异常时，将其健康状态上报到ZK，再被Scheduler感知，进而触发切换流程。

同一Set的主从节点基于MySQL的Replcation复制协议实现主从同步。

管理端Scheduler Manger给ZK发命令，Agent watch ZK消息，收到消息后通过Socket给数据库发送指令。
Agent负责上报资源、状态线程、数据同步线程、扩容任务检测线程、表一致性校验线程、binlog镜像备份线程。

特点是：主机可读可写，备机只读。主节点down机后，选出支持事务成功数最多的备节点进行切换，切换过程自动化。底层还是依赖MySQL，外接了一个同步协调器。

## 存储引擎
InnoDB存储引擎

## 索引

## 事务
分布式事务基于两阶段提交实现，并使用去中心化设计。

可能会遇到的问题：
* prepare超时
* commit log 写失败
* commit log 写超时
* commit log 超时或失败

对于写失败的错误由proxy直接往后端发xa rollback xid。对于超时错误则直接断开连接，由agent进行后续事务操作。

为了解决多节点的分布式事务，需要获得全局的分布式事务id，GTID。


## 锁


