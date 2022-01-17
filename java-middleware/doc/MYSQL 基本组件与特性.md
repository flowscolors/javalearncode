

参考文档：  
http://mysql.taobao.org/monthly/2021/10/02/
https://dev.mysql.com/doc/refman/8.0/en/show-processlist.html      

## MySQL基本组件
MySQL 主要分为 Server 层和引擎层，Server 层主要包括连接器、查询缓存、分析器、优化器、执行器，同时还有一个日志模块（binlog），这个日志模块所有执行引擎都可以共用，redolog 只有 InnoDB 有。

引擎层是插件式的，目前主要包括，MyISAM,InnoDB,Memory 等。



## MySQL命令执行过程
一次数据库的查询大体需要经历客户端，应用服务端，mysql服务端sql解析，优化器索引评估，执行计划生成，执行查询，返回给调用端。  



## MySQL线程
MYSQL是单进程多线程的结构，单top只能看到一行，需要top -Hp。  
MYSQL的线程主要分为两部分：系统线程（用来处理刷脏、读写数据的系统线程）、用户线程（处理用户SQL的线程）  

问题在于，对于用户线程，用户发送到MYSQL端执行的SQL，只会由一个用户线程来执行（one - thread - per -connection），所以MSYQL在处理复杂查询时，会出现"一核有难，多核围观"的尴尬场景。  
对于系统线程：  
一般来说多个系统线程很少能跑满CPU，除非MySQL有涉及相关的bug可能会影响。  

对于用户线程：  
一般大部分是由慢查询导致的，用户线程占用CPU时间过多，即用户线程占用了大量时间。可能有：  
1.MySQL进行长时间的计算，如:order by、group by、临时表、join等。这一类就是单纯查询效率不高，导致单个SQL占用CPU时间，也可能是数据量巨大导致。  
这个可以查看慢查询日志，或者使用SQL去进行查哪个SQL对应线程占用了大量CPU。

2.单纯就是QPS高，导致CPU时间被压满，比如4核服务器支持20K到30K的点查询，单个SQL占用时间不高，但是整体QPS很高。   



## MySQL特性
