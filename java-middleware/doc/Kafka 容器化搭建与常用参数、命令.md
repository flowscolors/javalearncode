## 1.容器化搭建Kafka



参考文档：  



## 2.常见需要修改的参数

### Consumer参数 
https://github.com/apache/kafka/blob/2.8/clients/src/main/java/org/apache/kafka/clients/consumer/ConsumerConfig.java

| 参数 | 默认值 | 推荐值 |说明 |
| :-----| :----: | :----: | :----: |
| auto.commit.enable | TRUE | FALSE |如果为真，consumer所fetch的消息的offset将会自动的同步到zookeeper。这项提交的offset将在进程无法提供服务时，由新的consumer使用。约束： 设置为false后，需要先成功消费再提交，这样可以避免消息丢失。 |
| auto.offset.reset | latest | earliest |没有初始化offset或者offset被删除时，可以设置以下值： earliest：自动复位offset为最早.latest：自动复位offset为最新。none：如果没有发现offset则向消费者抛出异常。anything else：向消费者抛出异常。 |
| connections.max.idle.ms | 600000 | 30000 |空连接的超时时间，设置为30000可以在网络异常场景下减少请求卡顿的时间。 |


消费者负责订阅Kafka的主题Topic，并从订阅的Topic的主题上拉取消息。与其他一些消息中间件不同，有一层是消费者组。消费者组的概念提升了吞吐量，但是也引入了再均衡的问题。
再均衡的问题在于，整个再均衡过程中，所有消费者都无法读取消息，这很致命。另一个问题在于分区被分配给新的消费者时，消费者当前状态会消失，如果当时未提交，会出现重复消费。

消费会被投递到订阅主题的消费者组的中的每一个消费者，但是注意不会重复投递，也即partition和消费者至多一一对应。消费者可以使用subscribe、unsubscribe，使用基于集合、正则、指定分区的订阅方式。
注意使用subscribe方法订阅的主题具有消费者自动再均衡的功能，而使用assign方法订阅的分区则没有这个功能。

Kafka的消费是基于拉取模式，需要客户端自己反复调用poll()方法，并返回offset。推荐使用带超时时间的poll()方法。

消费者的消息类型是ComsumerRecord，其中类型除了key、value还有topic、partition、offset、timestamp、headers、checksum、序列化器等。

消费者的客户端除了核心的消息拉取，还有消费位移、消费者协调器、组协调器、消费者选举、分区分配的分发、再均衡、心跳等功能。夏普非洲的offset提交之前是保存在zookeeper，现在是保存在内部的一个kafka主题_consumer_offset内。

但问题的关键是消息的拉取的基于poll()犯法，而这个poll()方法对于开发人员是一个黑盒，无法精确掌握其消费的起始位置。而有时需要更细粒度的掌握，这时可以使用seek(),可以追前或回溯消息。

KafkaConsumer 是非线程安全的。所以KafkaConsumer中定义了一个acquire()方法，用来检测是否当前只有一个线程在操作。KafkaConsumer的所有公用方法都会执行acquire()，除了wakeup()方法。

### Producer参数
https://github.com/apache/kafka/blob/2.8/clients/src/main/java/org/apache/kafka/clients/producer/ProducerConfig.java

| 参数 | 默认值 | 推荐值 |说明 |
| :-----| :----: | :----: | :----: |
| acks | 1 | 高可靠：all 高吞吐：1 | 收到Server端确认信号个数，表示procuder需要收到多少个这样的确认信号，算消息发送成功。acks参数代表了数据备份的可用性。常用选项：
acks=0：表示producer不需要等待任何确认收到的信息，副本将立即加到socket buffer并认为已经发送。没有任何保障可以保证此种情况下server已经成功接收数据，同时重试配置不会发生作用（因为客户端不知道是否失败）回馈的offset会总是设置为-1。
 acks=1：这意味着至少要等待leader已经成功将数据写入本地log，但是并没有等待所有follower是否成功写入。如果follower没有成功备份数据，而此时leader又无法提供服务，则消息会丢失。
 acks=all：这意味着leader需要等待所有备份都成功写入日志，只有任何一个备份存活，数据都不会丢失。 |
| retries | 0 | 结合实际业务调整 |客户端发送消息的重试次数。值大于0时，这些数据发送失败后，客户端会重新发送。注意，这些重试与客户端接收到发送错误时的重试没有什么不同。允许重试将潜在的改变数据的顺序，如果这两个消息记录都是发送到同一个partition，则第一个消息失败第二个发送成功，则第二条消息会比第一条消息出现要早。 |
| request.timeout.ms | 结合实际业务调整 | 30000 |设置一个请求最大等待时间，超过这个时间则会抛Timeout异常。超时时间如果设置大一些，如127000（127秒），高并发的场景中，能减少发送失败的情况。 |

max.request.size  生产者客户端能发送的消息的最大值。

compression.type  用来指定消息的压缩方式，默认值为none。该参数可以配置为 gzip、snappy、lz4

生产者发送的是消息体 ProducerRecord<K,V> 除了key，value其中还有topic、partition、headers、timestamps字段。其构造方法最简单的只需要Topic和value。当然你可以添加更多构造方法。

生产者发送可以使用 发后即忘 send()、同步send().get()、异步 send(record,new Callback(@Override))

KafkaProducer 是线程安全的，


### Broker参数
message.max.bytes  该参数用来指定broker所能接受的消息的最大值，默认值约1M。如果Producer发送的消息大于这个参数，则Pruducer会报异常。注意如果需要改动这个值，那么还需要考虑
                   max.request.size 客户端参数、max.message.bytes topic端参数，需要级联修改。


重分配是Broker端的问题，当某个broker下线或宕机，其上所有分区不可用，会进行leader、follower的重分配，其基本原理是通过控制器为每个分区添加新副本，新的副本会copy所需的数据，由于需要网络IO进行传输，需要占用额外资源。
可以使用相应脚本、broker端来实现复制限流。


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

## 生产者 - 分区器
消息在通过send()发往broker的过程中，可能会经过拦截器、序列化器和分区器等一系列操作才能真正发往broker。当然其中序列化器是必须的。
而分区器则是在ProduceRecord中partition为null时会使用，默认会对key进行hash来计算分区号。当然我们也可以使用自定义的分区器。
