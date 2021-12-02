


## Hashmap底层实现
JDK 1.8中使用位桶+链表+红黑树




## HashMap初始化长度、扩容和缩容
初始化长度length(默认值是16)，Load factor为负载因子(默认值是0.75)

扩容:


缩容: 无。 也正是因为Java的hashmap无缩容的机制，所以可能出现大量数据入队出队后，留下很多空的node节点。

PS:java的map不允许，go的map有这种操作。但是其实也是伪操作，实际要缩减还是要手动缩容。

参考文档：  
https://www.shuzhiduo.com/A/rV57qYAGzP/
https://www.zhihu.com/question/366679456

## hashmap并发的问题
