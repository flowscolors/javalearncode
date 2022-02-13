
ZooKeeper做为分布式协调中间件。是一个争对大型分布式系统的可靠协调系统，提供功能包括配置维护、命名服务、分布式同步、组服务等。

比如用来生产分布式ID、集群节点的命名服务、分布式锁。

## Zookeeper客户端
官方客户端、ZKCLient都有一些不足，比如缺少重试机制、社区不活跃、封装不够好，所以基本使用Netflix开源的Curator客户端。

分布式事件监听，在Curator的API中，事件监听有两种模式：

第一种是标准的观察者模式，通过watcher监听器去实现。
可以监听NodeCreated、NodeDeleted、NodeDataChanged、NodeChildChanged等。


第二种是缓存监听机制，通过引入一种本地缓存试图Cache机制去实现。这种监听机制可以理解为本地缓存视图与远程ZooKeeper视图的对比过程。
本地是缓存了所有数据，并且每次感知到Zookeepr的Znode状态变化就会触发事件。并且这种方式提供了事件监听器反复注册的能力，而watcher只能监听一次。
这种机制其实是通过本地和远程的对比，来触发相应的事件。
引入的缓存类型有：NodeCahce、PathCache、TreeCache