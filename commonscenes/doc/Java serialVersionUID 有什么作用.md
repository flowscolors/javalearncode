## 1.序列化与反序列化
通过序列化可以持续保存对象信息，将对象转换成字节数组后可以存储到文件、数据库、Redis、发送到消息队列等操作。并通过反序列化获得对象。

Java序列化API为处理对象序列化提供了一个标准机制，按照这个机制即可实现序列化。
1.使用Java中默认的序列化器，需要实现 java.io.Serializable 接口。
2.序列化、反序列化需要保证序列化的ID一致，一般使用 private static final long serialVersionUID 定义序列化ID。
3.序列化不保存静态遍历
4.需要序列化父类接口时，父类也需要实现 Serializable 接口。

实际除了Java原生的序列化，还有很多序列化框架，如 arvo、protobuf、thrift、fastjson 等。

## 2.关键字
transient  关键字 配合序列化接口使用，用来实现反序列化
即被 transient 修饰的变量，在反序列化后，transient 变量的值会被设置为对应类型的初始值。例如int类型的值为0，对象类型的值为null。



## 3.Kafka的序列化
消息在网络中传输使用需要把对象转换成字节数组。对于消息队列来说，生产者需要使用序列化器把对象（无论是String还是Map）转成字节数组。
消费者需要使用反序列化器把字节数组转转换成对象。而序列化器、反序列化器都可以使用Kafka客户端自带的序列化、反序列化器。也可以使用自定义序列器。






参考文档：  
https://www.chuckfang.com/2020/10/09/What-does-the-Java-serialVersionUID-do/