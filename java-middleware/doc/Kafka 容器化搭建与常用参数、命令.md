## 1.容器化搭建Kafka



参考文档：  



## 2.常见需要修改的参数

Consumer参数 
https://github.com/apache/kafka/blob/2.8/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java

| 参数 | 默认值 | 推荐值 |说明 |
| :-----| :----: | :----: | :----: |
| auto.commit.enable | TRUE | FALSE |如果为真，consumer所fetch的消息的offset将会自动的同步到zookeeper。这项提交的offset将在进程无法提供服务时，由新的consumer使用。约束： 设置为false后，需要先成功消费再提交，这样可以避免消息丢失。 |
| auto.offset.reset | latest | earliest |没有初始化offset或者offset被删除时，可以设置以下值： earliest：自动复位offset为最早.latest：自动复位offset为最新。none：如果没有发现offset则向消费者抛出异常。anything else：向消费者抛出异常。 |
| connections.max.idle.ms | 600000 | 30000 |空连接的超时时间，设置为30000可以在网络异常场景下减少请求卡顿的时间。 |



Producer参数
https://github.com/apache/kafka/blob/2.8/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java

| 参数 | 默认值 | 推荐值 |说明 |
| :-----| :----: | :----: | :----: |
| acks | 1 | 高可靠：all 高吞吐：1 | 收到Server端确认信号个数，表示procuder需要收到多少个这样的确认信号，算消息发送成功。acks参数代表了数据备份的可用性。常用选项：
acks=0：表示producer不需要等待任何确认收到的信息，副本将立即加到socket buffer并认为已经发送。没有任何保障可以保证此种情况下server已经成功接收数据，同时重试配置不会发生作用（因为客户端不知道是否失败）回馈的offset会总是设置为-1。
 acks=1：这意味着至少要等待leader已经成功将数据写入本地log，但是并没有等待所有follower是否成功写入。如果follower没有成功备份数据，而此时leader又无法提供服务，则消息会丢失。
 acks=all：这意味着leader需要等待所有备份都成功写入日志，只有任何一个备份存活，数据都不会丢失。 |
| retries | 0 | 结合实际业务调整 |客户端发送消息的重试次数。值大于0时，这些数据发送失败后，客户端会重新发送。注意，这些重试与客户端接收到发送错误时的重试没有什么不同。允许重试将潜在的改变数据的顺序，如果这两个消息记录都是发送到同一个partition，则第一个消息失败第二个发送成功，则第二条消息会比第一条消息出现要早。 |
| request.timeout.ms | 结合实际业务调整 | 30000 |设置一个请求最大等待时间，超过这个时间则会抛Timeout异常。超时时间如果设置大一些，如127000（127秒），高并发的场景中，能减少发送失败的情况。 |



Topic参数





参考文档：  
https://kafka.apache.org/documentation/#configuration

## 3.客户端使用规范
consumer使用规范
1. consumer的owner线程需确保不会异常退出，避免客户端无法发起消费请求，阻塞消费。
2. 确保处理完消息后再做消息commit，避免业务消息处理失败，无法重新拉取处理失败的消息。
3. consumer不能频繁加入和退出group，频繁加入和退出，会导致consumer频繁做rebalance，阻塞消费。
4. consumer数量不能超过topic分区数，否则会有consumer拉取不到消息。
5. consumer需周期poll，维持和server的心跳，避免心跳超时，导致consumer频繁加入和退出，阻塞消费。
6. consumer拉取的消息本地缓存应有大小限制，避免OOM（Out of Memory）。
7. consumer session设置为30秒，session.timeout.ms=30000。
8. Kafka不能保证消费重复的消息，业务侧需保证消息处理的幂等性。
9. 消费线程退出要调用consumer的close方法，避免同一个组的其他消费者阻塞sesstion.timeout.ms的时间。

producer使用规范
1. 同步复制客户端需要配合使用：acks=all
2. 配置发送失败重试：retries=3
3. 发送优化：linger.ms=0
4. 生产端的JVM内存要足够，避免内存不足导致发送阻塞

topic使用规范

1.配置要求：推荐3副本，同步复制，最小同步副本数为2，且同步副本数不能等于topic副本数，否则宕机1个副本会导致无法生产消息。

2.创建方式：支持选择是否开启kafka自动创建Topic的开关。选择开启后，表示生产或消费一个未创建的Topic时，会自动创建一个包含3个分区和3个副本的Topic。

3.单topic最大分区数建议为100。


其他建议
* 连接数限制：3000

* 消息大小：不能超过10MB

* 使用sasl_ssl协议访问Kafka：确保DNS具有反向解析能力，或者在hosts文件配置kafka所有节点ip和主机名映射，避免Kafka client做反向解析，阻塞连接建立。

* 磁盘容量申请超过业务量 * 副本数的2倍，即保留磁盘空闲50%左右。

* 业务进程JVM内存使用确保无频繁FGC，否则会阻塞消息的生产和消费。

参考文档：  
https://support.huaweicloud.com/bestpractice-kafka/Kafka-client-best-practice.html