
Redis内部使用一个redisObject对象来标识所有的key和value数据，redisObject最主要的信息如图所示：type代表一个value对象具体是何种数据类型，encoding是不同数据类型在Redis内部的存储方式，比如——type=string代表value存储的是一个普通字符串，那么对应的encoding可以是raw或是int，如果是int则代表世界Redis内部是按数值类型存储和表示这个字符串。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/Redis-object.JPG)

字符串 type string  | encoding  raw/int
对象   type object  | encoding
字典   type hash
列表   type list
集合   type set
有序集合 type sortedset

### String
简单的set get，做kv操作。字符串的值可以是字符串、数字、二进制、但是值最大不能超过512M。
内部编码有3种：
int ： 8个字节的长整型。
embstr： 小于等于39字节的字符串。
raw：大于39字节的字符串。



### object

### hash
类似map结构，可以存结构化数据。
内部编码有2种：
ziplist ： 哈希类型元素个数少于值（512），所有值大小小于值（64字节），Redis会使用ziplist做为哈希的内部实现。
hashtable： 不满足ziplist时，使用hashtable。

### list
有序列表，可用来存列表型的数据结构，类似粉丝列表、文章评论列表。使用lrange命令，可以读取某个闭区间内元素，可以实现类似分页查询功能。
内部编码有2种：
ziplist ： 哈希类型元素个数少于值（512），所有值大小小于值（64字节），Redis会使用ziplist做为list的内部实现。
linkedlist： 不满足ziplist时，使用linkedlist。

### set
无序集合，自动去重。可以基于set进行交集、并集、差集计算。比如看共同好友。集合间运算在元素较多时会比较耗时。
求多个集合交集 sinter key [key···]
求多个集合并集 sunion key [key···]
求多个集合差集 sdiff key [key···]

内部编码有2种：
intset ： 哈希类型元素个数少于值（512），Redis会使用intset做为set的内部实现。
hashtable： 不满足intset时，使用hashtable。

### sortedset
排序的 set，去重但可以排序，写进去的时候给一个分数，自动根据分数排序。

内部编码有2种：
ziplist ： 哈希类型元素个数少于值（128），所有值大小小于值（64字节），Redis会使用ziplist做为sortedset的内部实现。ziplist可以有效减少内存使用。
skiplist： 不满足ziplist时，使用skiplist。

