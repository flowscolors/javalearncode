## 1.Kafka如何保证消息顺序消费
1.Kafka保证partion有序，那你一个topic设一个partion即可。（为了顺序读写，这是肯定的）  
2.Kafka发送时可以指定partion，那你使用key保证所有对象发一个partion即可。

## 2.Kafka怎么保证不重复消费


## 3.Kafka怎么做事务消息
1.官方文档不提供  
2.本来设计思想就没有往这方面去做，保证高吞吐量，而非有效性。  
