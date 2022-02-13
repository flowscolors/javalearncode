
Redis集群使用，先过一遍官网文档。https://redis.io/topics/cluster-tutorial

## Redis集群设计的主要特性与基本原理
https://redis.io/topics/cluster-tutorial
Redis集群的设计目的：
Redis CLuster提供了一种Redis安装方式，其中数据自动分片到多个Redis节点。
Redis Cluster还在分区间提供了一定的可用性，即在某些节点故障或无法通信时继续操作的能力。但是当大多数节点发生故障时，集群将停止运行。

因此Redis Cluster的能力就是：1.多节点间自动拆分数据集的能力。 2.当节点的子集遇到故障或无法继续通信时的继续操作能力。



## 创建和使用Redis集群
https://redis.io/topics/cluster-tutorial

* Redis集群TCP端口
每个Redis集群都需要打开两个TCP端口，普通Redis服务的6379数据端口，用做集群节点通信的的10000+数据端口，16379.

目前 Redis Cluster 不支持经过 NAT 处理的环境以及重新映射 IP 地址或 TCP 端口的一般环境。比如docker的默认网络，使用host网络可以。

* Redis集群创建
首先需要几个cluster-enabled开启的空Redis实例，使用Redis-CLi命令创建集群。
```shell script
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 \
127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 \
--cluster-replicas 1
```
这里使用的命令是create，因为我们要创建一个新集群。该选项--cluster-replicas 1意味着我们希望为每个创建的主服务器创建一个副本。其他参数是我想用来创建新集群的实例的地址列表。

显然，符合我们要求的唯一设置是创建一个具有 3 个主服务器和 3 个副本的集群。

Redis-cli 会建议你一个配置。输入yes接受建议的配置。集群将被配置并加入，这意味着实例将被引导到彼此交谈。最后，如果一切顺利，你会看到这样的消息：

```shell script
[OK] All 16384 slots covered
```

这意味着至少有一个主实例为 16384 个可用插槽中的每一个提供服务。

如果不使用redis-cli命令行，则可以直接使用create-cluster脚本创建集群，这会隐藏一些创建集群的步骤。

* 重新分片
重新分片基本上意味着将哈希槽从一组节点移动到另一组节点，并且与集群创建一样，它是使用 redis-cli 实用程序完成的。
只要指定一个节点，Redis会自动找到其他节点。可以使用交互的方式或非交互的方式完成重hash。
```shell script
redis-cli --cluster reshard 127.0.0.1:7000

$ redis-cli -p 7000 cluster nodes | grep myself
97a3a64667477371c4479320d683e4c8db5858b1 :0 myself,master - 0 0 0 connected 0-5460

redis-cli --cluster check 127.0.0.1:7000

redis-cli --cluster reshard <host>:<port> --cluster-from <node-id> --cluster-to <node-id> --cluster-slots <number of slots> --cluster-yes
```


* Redis集群配置
 Redis Cluster 引入的配置参数redis.conf
 cluster-enabled<yes/no>：如果是，则在特定 Redis 实例中启用 Redis Cluster 支持。否则，实例将像往常一样作为独立实例启动。
 
 cluster-config-file<filename>：请注意，尽管有此选项的名称，但这不是用户可编辑的配置文件，而是 Redis Cluster 节点在每次发生更改时自动持久化集群配置（基本上是状态）的文件，为了能够在启动时重新读取它。该文件列出了集群中的其他节点、它们的状态、持久变量等内容。由于某些消息接收，此文件通常会被重写并刷新到磁盘上。
 
 cluster-node-timeout<milliseconds>：Redis 集群节点不可用的最长时间，而不被视为失败。如果主节点在超过指定的时间内无法访问，它将由其副本进行故障转移。此参数控制 Redis Cluster 中的其他重要内容。值得注意的是，在指定时间内无法到达大多数主节点的每个节点都将停止接受查询。
 
 cluster-migration-barrier<count>：master 将保持连接的最小副本数，以便另一个副本迁移到不再被任何副本覆盖的 master。有关更多信息，请参阅本教程中有关副本迁移的相应部分。
 
 

## Redis分区
https://redis.io/topics/partitioning

Redis CLuster并未使用一致性hash实现分片，而是使用环形hash的方式。其中每个键都是我们所谓的散列槽的一部分。
Redis 集群中有 16384 个哈希槽，要计算给定键的哈希槽是多少，我们只需将键的 CRC16 取模 16384。Redis 集群中的每个节点都负责哈希槽的子集，比如 Ａ　０～５５００，Ｂ　５５０１～１１０００，Ｃ　１１００１～１６３８３

添加节点时会把部分槽从ＡＢＣ移动到Ｄ。删除时则把Ａ的槽移动到ＢＣ。理论上ｈａｓｈ槽的移动不需要停机时间，因为数据都在槽里，可以被访问到。

但是在节点发生故障时，如果B发生故障，集群将无法继续，因为我们不再有办法为 5501-11000 范围内的哈希槽提供服务。所以一般会为每个节点配置master slave，salve复制master，并在master失败时提升为master。如果master slave同时失效，则无法继续提供服务。

所以也因此Redis并不是一个强一致的系统，而是最终一致的系统，AP。因为集群master slave的写入默认不是同步。

## Redis配置处理、传播、故障转移

