
Redis内部使用一个redisObject对象来标识所有的key和value数据，redisObject最主要的信息如图所示：type代表一个value对象具体是何种数据类型，encoding是不同数据类型在Redis内部的存储方式，比如——type=string代表value存储的是一个普通字符串，那么对应的encoding可以是raw或是int，如果是int则代表世界Redis内部是按数值类型存储和表示这个字符串。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/Redis-object.JPG)

字符串 type string  | encoding  raw/int
对象   type object  | encoding
字典   type hash
列表   type list
集合   type set
有序集合 type sortedset

### String
简单的set get，做kv操作。

### object

### hash
类似map结构，可以存结构化数据

### list
有序列表，可用来存列表型的数据结构，类似粉丝列表、文章评论列表。使用lrange命令，可以读取某个闭区间内元素，可以实现类似分页查询功能。

### set
无序集合，自动去重。可以基于set进行交集、并集、差集计算。比如看共同好友。

### sortedset
排序的 set，去重但可以排序，写进去的时候给一个分数，自动根据分数排序。



