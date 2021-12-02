
一般前端调用后端接口都有超时时间的，比如40s，如果后端调数据库超过这个时间则会报请求超时。  

## 1.诊断命令
1. show full processlist  
2. 查看慢SQL日志

MySQL 慢查询时间（long_query_time）的默认值是10s，在遇到性能问题时，若发现没有慢查询，建议将其参数调成1s ，再观察业务周期内的慢查询，进而对其慢查询进行优化。


## 特定

查询效率不高的慢查询通常有以下几种情况：

* 全表扫描：Handler_read_rnd_next 这个值会大幅度突增，且这一类查询在 slowlog 中 row_examined 的值也会非常高。
* 索引效率不高，索引选错了：Handler_read_next 这个值会大幅度的突增，不过要注意这种情况也有可能是业务量突增引起的，需要结合 QPS/TPS 一起看。这一类查询在 slowlog 中找起来会比较麻烦，row_examined 的值一般在故障前后会有比较明显的不同，或者是不合理的偏高。
** 比如数据倾斜的场景，一个小范围的 range 查询在某个特定的范围内 row_examined 非常高，而其他的范围时 row_examined 比较低，那么就可能是这个索引效率不高。
* 排序比较多：order by，group by 这一类查询通常不太好从 Handler 的指标直接判断，如果没有索引或者索引不好，导致排序操作没有消除的话，那么在 processlist 和 slowlog 通常能看到这一类查询语句出现的比较多。  

当然，不想详细的分析 MySQL 指标或者是情况比较紧急的话，可以直接在 slowlog 里面用 rows_sent 和 row_examined 做个简单的除法，比如 row_examined/rows_sent > 1000 的都可以拿出来作为“嫌疑人”处理。这类问题一般在索引方面做好优化就能解决。   
PS：1000 只是个经验值，具体要根据实际业务情况来定。


## 特定场景优化 
查询问题绝大多数都与索引有关。增删问题大多与锁有关。
 
1.对大批量order by 优化  
https://developer.aliyun.com/article/760998
https://blog.csdn.net/u014745069/article/details/104075503
https://blog.csdn.net/qq_40992849/article/details/108485643
https://cloud.tencent.com/developer/article/1478567

2.对联合索引进行优化

3.对left join进行优化，去掉临时表  
left join的使用，可以使用select进行预筛选，如果字段（列）很多，可以临时表筛选少部分


https://blog.csdn.net/zzhongcy/article/details/99645673  
https://zhuanlan.zhihu.com/p/103661924  
https://juejin.cn/post/6844903730546999309

4.特定SQL导致的问题
https://segmentfault.com/a/1190000003063737

5.对in 、not 进行优化
很明显我们都知道使用in 、not in是会进行全表扫描的，但某些业务场景又一定需要in 、not in。  
这种情况下如何进行优化呢，一般解决方案是用left join、inner join。  

http://www.piaoyi.org/database/mysql-not-in-left-join.html
https://www.cnblogs.com/hydor/p/5391556.html
https://segmentfault.com/a/1190000024431716
https://www.cnblogs.com/feiling/p/3393356.html