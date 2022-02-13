
以下参数基于MySQL 5.7.29版本讨论，5.6之前版本可能有部分参数不同，且含义不同。  

## 1.生产级参数配置表 
### 1）内存利用方面：
innodb_buffer_pool_size  
可以说Innodb最重要的参数了，该参数主要分配缓存Innodb表的索引、数据、插入数据时的缓冲。  
从磁盘读取数据效率是很低的，为了避免这个问题，MySQL 开辟了基于内存的缓冲池，核心做法就是把经常请求的热数据放入池中，如果请求交互的数据都在缓冲池中则会很高效。  
如果是一台DB专用服务器，可占用到内存的70% ~ 80%，但是注意不能过大，如果到SWAP，则速度又会变慢。  
默认128M，但是可动态调节该参数。SET GLOBAL innodb_buffer_pool_size=402653184; 注意动态调整期间，所有用户请求将会阻塞。  

### 2）日志方面： 
innodb_log_buffer_size：  
作用：事务在内存中的缓冲，也就是日志缓冲区的大小， 默认设置即可，具有大量事务的可以考虑设置为16M

innodb_flush_logs_at_trx_commit  
作用：控制事务的提交方式,也就是控制log的刷新到磁盘的方式。
这个参数只有3个值（0，1，2）.默认为1，性能更高的可以设置为0或是2，这样可以适当的减少磁盘IO（但会丢失一秒钟的事务。） 。其中：  
0：log buffer中的数据将以每秒一次的频率写入到log file中，且同时会进行文件系统到磁盘的同步操作，但是每个事务的commit并不会触发任何log buffer 到log file的刷新或者文件系统到磁盘的刷新操作；
1：（默认为1）在每次事务提交的时候将logbuffer 中的数据都会写入到log file，同时也会触发文件系统到磁盘的同步；
2：事务提交会触发log buffer 到log file的刷新，但并不会触发磁盘文件系统到磁盘的同步。此外，每秒会有一次文件系统到磁盘同步操作。 

### 3）文件IO分配，空间占用方面
innodb_open_files
作用：限制Innodb能打开的表的数据。
分配原则：这个值默认是300。如果库里的表特别多的情况，可以适当增大为1000。innodb_open_files的大小对InnoDB效率的影响比较小。故障恢复时会有影响。  

innodb_data_home_dir
放置表空间数据的目录，默认在mysql的数据目录，设置到和MySQL安装文件不同的分区可以提高性能。


### 4） 锁相关部分
InnoDB_lock_wait_timeout
这个参数自动检测行锁导致的死锁并进行相应处理,但是对于表锁导致的死锁不能自动检测.默认值为50秒.

### 5）其它相关参数（适当的增加table_cache）
max_connections
设置最大连接（用户）数，每个连接MySQL的用户均算作一个连接，max_connections的默认值为100。此值需要根据具体的连接数峰值设定。
实际连接数可以使用 show status like 'Threads%' 查看
设置方法，在my.cnf文件里：
max_connections = 3000

query_cache_size
查询缓存大小，如果表的改动非常频繁，或者每次查询都不同，查询缓存的结果会减慢系统性能。可以设置为0。
设置方法，在my.cnf文件里：
query_cache_size = 512M

sort_buffer_size
connection级的参数，排序缓存大小。一般设置为2-4MB即可。
设置方法，在my.cnf文件里：
sort_buffer_size = 1024M
read_buffer_size
connection级的参数。一般设置为2-4MB即可。
设置方法，在my.cnf文件里：
read_buffer_size = 1024M

max_allowed_packet
网络包的大小，为避免出现较大的网络包错误，建议设置为16M
设置方法，在my.cnf文件里：
max_allowed_packet = 16M

thread_cache_size
线程缓存，如果一个客户端断开连接，这个线程就会被放到thread_cache_size中（缓冲池未满），SHOW STATUS LIKE 'threads%';如果 Threads_created 不断增大，那么当前值设置要改大，改到 Threads_connected 值左右。（通常情况下，这个值改善性能不大），默认8即可
设置方法，在my.cnf文件里：
thread_cache_size = 8

innodb_thread_concurrency
线程并发数，建议设置为CPU内核数*2
设置方法，在my.cnf文件里：


参考文档：
https://www.cnblogs.com/kevingrace/p/6133818.html

## 生产级别my.cnf配置

waiting code


## grafana参数解析
MySQL Uptime      MySQL启动时间
Currnet QPS       基于MySQL命令 show status; 的结果，计算最近1s的查询时间数目
InnnoDB Buffer Pool Size    InnoDB会保持一个为数据和索引的buffer pool的存储区域，属于影响MySQL性能的最重要参数之一。大部分情况下是数据库宿主机的60% ~ 90%。

Connections：
MySQL Connections 数据连接数，Max Connection默认值151.Max used 是曾经使用的最大的值。
MySQL Client Thread Activity    Thread Connected是open的连接数，Thread Running是没有sleeping的线程数。

Table Locks：
MySQL Questions  server端计算的statements数目。数目包括client发送到server端的statement，以及statement被存储但是没有被计算的。
MySQL Thread Cache  设置存储多少线程server端可以存cache用来重用。当一个client关闭连接，该客户端线程会被放到cahche，如果cache未满。这样来尽量复用来连接cache。
MySQL Select Types  大部分关系型数据库中，基于索引的查询会比全表扫描更有效。这里可以查看没有走索引的几种方式及其个数。Select Scan，代表全表扫描。Select Range，MySQL扫描了一个给定区域的所有行。Select Full Join， 代表join查询没有join到一个index，这通常会带来一个很大的查询。
MySQL Table  Locks  存储引擎对应的MySQL tables 表锁的数目。在InnoDB中大部分是行锁，只有少部分情况是表锁。最有效的情况是比较 Locks Immediate和 Locks Waited值。前者增长是因为失败的操作，后者增长是意味着由锁住的连接。

Sort：
MySQL Sorts      代表MySQL的sort操作，包括order或者desc等命令，因为这些sort操作导致无法走index，从而导致全表扫描或者范围扫描。
MySQL Slow Querues 代表MySQL的慢查询操作，阈值基于 long_query_time

Memory： MySQL Internal Memeory Overview 
System Memory    系统内存容量
InnoDB Buffer Pool Data  InnoDB Buffer容量，存储cache数据和索引
InnoDB Log Buffer Size   InnoDB log Buffer 提供运行事务运行，在事务提交之前不必一定写入磁盘的日志，写入该log buffer即可，默认128M。

Command，Handles，Processed：
Top Command Counters     统计每种操作的次数 select、update、insert、set_option、commit ··· 
Top Command Counters Hourly 统计每种操作的次数，每小时统计数据。
MySQL Handlers           MySQL 统计数据会统计MySQL进行 select、update、insert、set_option ··· 的操作
MySQL Transaction Handlers  MySQL 统计数据会统计MySQL进行 commit 、 rollback ··· 的操作