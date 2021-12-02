

相关实验：  
https://segmentfault.com/a/1190000021464570

## 1.Mysql索引类型
从逻辑类型上划分（即一般创建表时设置的索引类型）：

唯一索引（UNIQUE）：索引列的值必须唯一，但允许有空值。如果是组合索引，则列值的组合必须唯一。
主键索引（PRIMARY）：一种特殊的唯一索引，一个表只能有一个主键，不允许有空值。一般是在建表的时候同时创建主键索引。
普通索引（INDEX）：最基本的索引，没有任何限制。
组合索引：多个字段上创建的索引，只有在查询条件中使用了创建索引时的第一个字段，索引才会被使用。使用组合索引时遵循最左前缀集合。

从物理存储上划分：

聚集索引(Clustered)：表中各行的物理顺序与键值的逻辑（索引）顺序相同，每个表只能有一个。
非聚集索引(Non-clustered)：非聚集索引指定表的逻辑顺序，也可以视为二级索引。数据存储在一个位置，索引存储在另一个位置，索引中包含指向数据存储位置的指针。可以有多个，小于 249 个。




* 聚簇索引(主键索引)  聚簇索引是叶子节点保存了数据
普通索引  普通索引的叶子节点保存的是数据地址，大部分是需要两遍查询 Explain 为null，走索引覆盖只需要一次 Explain 为index。

聚簇索引不是一种单独的索引类型，而是一种数据存储方式。具体细节依赖于实现方式。如 InnoDB 的聚簇索引实际是在同一个结构中保存了 B 树的索引和数据行。

聚簇表示数据行和相邻的键值紧凑地存储在一起，因为数据紧凑，所以访问快。因为无法同时把数据行存放在两个不同的地方，所以一个表只能有一个聚簇索引。

若没有定义主键，InnoDB 会隐式定义一个主键来作为聚簇索引。

* 索引覆盖/覆盖索引
索引包含所有需要查询的字段的值。因为索引条目通常远小于数据行的大小，所以若只读取索引，能大大减少数据访问量。

对于 InnoDB 引擎，若辅助索引能够覆盖查询，则无需访问主索引。

走索引查询数据时，如果该索引已经包含需要的数据，则称之为索引覆盖。若索引中不能拿到想要的数据，则需要通过主键拿一整行数据，这个过程叫回表，需要一次 IO 操作，所以我们写 SQL 时尽量使用索引覆盖，降低 IO 开销。



## 2.创建、删除索引命令

```
SHOW INDEX FROM table_name;
ALTER TABLE testalter_tbl ADD INDEX (c);
ALTER TABLE testalter_tbl DROP PRIMARY KEY;

CREATE INDEX indexName ON table_name (column_name)
DROP INDEX [indexName] ON mytable; 
```

PS:索引值可以为null吗？


## Explain命令使用
Explain命令使用，desc命令也可以类似explain命令，展示执行计划。

参考文档：
https://m.linuxidc.com/Linux/2018-06/152757.htm
https://blog.csdn.net/justry_deng/article/details/81458470

* table  table 显示的是这一行的数据是关于哪张表的。  

* type   这是重要的列，显示连接使用了何种类型，类型还是蛮多的，最不理想的 ALL 意味对于查询的表进行全表数据扫描，应该尽量避免。  
       一般来说，得保证type达到range级别，最好能达到ref。

system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL

const  代表表中一个记录的最大值能匹配这个查询（索引可以是主键或唯一索引），因为只有一行，所以这个值是一个常数，因为MYSQL先读这个值，并把这个值当常数对待。

ref    代表对于每个来自于前面的表的行组合,所有有匹配索引值的行将从这张表中读取。

index  代表全索引扫描，其实和全表扫描差不多，只是扫描时候按照索引顺序进行而不是依赖行进行，优点是避免排序，但是开销依旧很大。  


* key    表示实际使用的索引。如果为 Null，则没有使用索引，这种情况也是尤其需要注意的。  

* rows   表明 SQL 返回请求数据的行数。这里只是优化器给出的预估值，不一定准确。  

* ref    表面SQL使用了索引的哪些列

参考文档：  
https://mengkang.net/1124.html

* filtered 
表示返回结果的行数占需读取行数的百分比。部分认为Filtered列的值越大越好（值越大，表明实际读取的行数与所需要返回的行数越接近）。不全面。
 
参考文档：  
https://qastack.cn/dba/164251/what-is-the-meaning-of-filtered-in-mysql-explain

* extra

关于 extra，Using filesort、Using temporary属于需要注意的状态，因为这样的状态大部分是会对性能产生不良的影响，意味着查询需要优化了。

**Using where：**表示**** 优化器需要通过索引回表查询数据。一般是select中有索引不涉及的字段。

**Using index：**表示**** 覆盖索引，表示直接访问索引就能获取到足够的数据，不需要进行索引回表，性能应该是最好的了。  

**Using index condition：**表示**** 索引下推，MYSQL5.6引入的新特性，是MYSQL为了减少回表的重要优化。先条件过滤索引，过滤完索引后找到所有符合索引条件的数据行，随后用 WHERE 子句中的其他条件去过滤这些数据行。是一种在存储引擎层使用索引过滤数据的一种优化方式。     

**Using filesort：**表示****SQL 需要进行额外的步骤来发现如何对返回的行排序。它会根据连接类型、存储排序键值和匹配条件的全部行进行排序。
> Using filesort：
  MySQL must do an extra pass to find out how to retrieve the rows in sorted order. The sort is done by going through all rows according to the join type and storing the sort key and pointer to the row for all rows that match the WHERE clause.
  Mysql需要额外的一次传递，以找出如何按排序顺序检索行，通过根据联接类型浏览所有行并为所有匹配where子句的行保存排序关键字和行的指针来完成排序，然后关键字被排序，并按排序顺序检索行。
  

**Using temporary：**表示****MySQL 需要创建一个临时表来存储结果，非常消耗性能。
> 从磁盘中查询所需的列，按照order by列在buffer中对它们进行排序，然后扫描排序后的列表进行输出。它的效率更高一些，避免了第二次读取数据，并且把随机I/O变成了顺序I/O，但是会使用更多的空间，因为它把每一行都保存在内存中了。但当读取数据超过sort_buffer的容量时，就会导致多次读取数据，并创建临时表，最后多路合并，产生多次I/O，反而增加其I/O运算。 
> 一般看到Using temporary 就需要增大增加sort_buffer_size、max_length_for_sort_data参数的设置。

## explain analyzes使用


## profiling命令使用

很方便 只有本机执行的SQL才会进行记录。不会有异常的SQL来影响。  
查完大多数时间是在sending data，Block_ops_out很大。  

```
mysql> set profiling=1;     开启profiling参数
mysql> show profiles;       获取系统中保存的所有 Query 的 profile 概要信息
mysql> show profile cpu,block io for query 3;    针对单个 Query 获取详细的 profile 信息
```

参考文档： 
https://segmentfault.com/a/1190000020399424
https://www.cnblogs.com/ggjucheng/archive/2012/11/15/2772058.html

## 智能索引分析工具使用
美团有SQLAdvise 小米有Soar，使用二进制命令来分析SQL。  


## 索引失效
### 违反最左前缀法则
如果索引有多列，要遵守最左前缀法则（组合索引）
即查询从索引的最左前列开始并且不跳过索引中的列
explain select * from user where age = 20 and phone = '18730658760' and pos = 'cxy';



## order by使用场景下
Mysql 有两种方式可以生成排序结果：通过排序操作；或者按索引顺序扫描。

索引最好既满足排序，又用于查找行。这样，就可以使用索引来对结果排序。

### order by索引失效 1
实际操作中，如果order by的字段在where条件中，则速度很快，否则会慢一个量级。  
为什么只有order by 字段出现在where条件中时,才会利用该字段的索引而避免排序。这和数据库读取数据有关。  

一条SQL实际上可以分为三步。 1.得到数据  2.处理数据   3.返回处理后的数据 
> select sid from zhuyuehua.student where sid < 50000 and id < 50000 order by id desc

第一步：根据where条件和统计信息生成执行计划，得到数据。 

第二步：将得到的数据排序。 
当执行处理数据（order by）时，数据库会先查看第一步的执行计划，看order by 的字段是否在执行计划中利用了索引。如果是，则可以利用索引顺序而直接取得已经排好序的数据。如果不是，则排序操作。 

第三步：返回排序后的数据。 

因为索引是有序的，所以当order by 中的字段出现在where条件中时，才会利用索引而不排序，更准确的说，order by 中的字段在执行计划中利用了索引时，不用排序操作。其他需要排序的操作 比如group by 、union 、distinct等也是如此。


### order by索引失效 2
select * 在SELECT中查询了索引建以外的列，那么ORDER BY就不会使用索引了。你可以用FORCE INDEX来强制使用索引。
还有一点，就是所谓的覆盖索引。覆盖索引的定义是：MySQL可以根据索引返回select字段而不用根据索引再次查询文件而得出结果。
         
参考文档： 
https://blog.csdn.net/qq_36176985/article/details/72782657
https://blog.csdn.net/weixin_44222272/article/details/106724773

PS:如何正确使用order by？
https://blog.csdn.net/ryb7899/article/details/5580624
https://www.cnblogs.com/yqzc/p/12541917.html
https://www.cnblogs.com/songwenjie/p/9418397.html
https://cloud.tencent.com/developer/article/1803691


### order by索引 排序效率
order by是排序，而索引就是有序的，所以如果我能够按照索引进行排序，排序就是很方便的。故前提：
1） 排序列必须有B-tree索引
2） 如果是多表联合查询，排序列必须是对驱动表字段的排序


参考文档：  
https://developer.aliyun.com/article/760998




### 驱动表与STRAIGHT_JOIN
在使用inner join联接语句时，MySQL表关联的算法是 Nest Loop Join（嵌套联接循环），Nest Loop Join就是通过两层循环手段进行依次的匹配操作，最后返回结果集合。  

数据量小的会是驱动表，并且驱动表字段可以排序，但是对于非驱动表则要循环查询（创建临时表进行排序），如果order by非驱动表，则会产生临时表。
参考文档：  
https://blog.csdn.net/canot/article/details/104920558

### 隐式类型转换  
表结构中类型是 varchar，SQL 中用的 int

### 模糊匹配开头  
由于 MySQL 最左匹配原则，所以查询条件模糊开头无法命中索引

### 使用or       
直接写 or 查询两个字段无法使用索引，这种场景，我们可以将 or 改写成 union 即可。



## 模拟数据
500w的SQL要很大的文件了，使用存储过程直接生成。  
https://cloud.tencent.com/developer/article/1638038
https://database.51cto.com/art/202103/650144.htm  
https://blog.objectspace.cn/2019/09/04/%E3%80%90%E4%BB%8E%E5%85%A5%E9%97%A8%E5%88%B0%E5%85%A5%E5%9C%9F%E3%80%91%E4%BB%A4%E4%BA%BA%E8%84%B1%E5%8F%91%E7%9A%84%E6%95%B0%E6%8D%AE%E5%BA%93%E5%BA%95%E5%B1%82%E8%AE%BE%E8%AE%A1/
http://learn.lianglianglee.com/%E4%B8%93%E6%A0%8F/%E8%AF%B4%E9%80%8F%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95/19%20%20%E5%A6%82%E4%BD%95%E6%A0%B9%E6%B2%BB%E6%85%A2%20SQL%EF%BC%9F.md