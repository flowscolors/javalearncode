## 1.数据库CPU 100%排查思路
已经到出现问题了，用什么命令去排查。
* 查看表是否在使用
> show open table where in_use > 0;  //查询是否表锁 Name_locked表示表是否被锁，0是没有锁定，1为有写锁 。 In_use显示出有多少线程正在使用此表，有可能已经给这个表上锁，或者等待获得锁，然后给这个表上锁;

* 查看数据库当前的进程，看一下有无正在执行的慢SQL记录线程。
> show full processlist; //查看当前线程详情,Time代表执行秒数，可以看一下有无正在执行的慢SQL记录线程。
> show processlist展示的内容是从information_schema.processlist数据表查询得到。可以通过select这个表做条件。
> select * from information_schema.processlist where host like 'IP%';
> select * from information_schema.processlist where command = 'Query';

* 查看当前运行的所有事务
> select * from information_schema.innodb_trx // 查询运行的事务，是否被锁住

* 查看当前出现的锁
> select * from information_schema.innodb_locks // 查看当前锁定的事务

* 查看锁等待的对应关系
> select * from information_schema.innodb_locks_waits //查看当前等待锁的事务

* 批量删除事务表中的事务
通过information_schema.processlist表中的连接信息生成需要处理掉的MySQL连接的语句临时文件，然后执行临时文件中生成的指令。
```
SELECT concat('KILL ',id,';') 
FROM information_schema.processlist p 
INNER JOIN  information_schema.INNODB_TRX x 
ON p.id=x.trx_mysql_thread_id 
WHERE db='test';

```
去操作系统执行查出来的kill命令即可。

* 查看占用CPU最高的SQL
```
 /查看占用CPU最高的SQL
top -Hp pid 

//根据操作系统线程ID，查看mysql数据库中对应的线程ID
SELECT THREAD_ID,NAME,PROCESSLIST_ID,thread_os_id from performance_schema.threads where thread_os_id = 74;

//根据mysql数据库的线程ID获取sql
select * from performance_schema.events_statements_current WHERE THREAD_ID = 105;
```

* 查看缓存命中
> show status like  'Innodb_buffer_pool_%';
> 1.（Innodb_buffer_pool_read_requests - Innodb_buffer_pool_reads） / Innodb_buffer_pool_read_requests * 100%。一般来讲这个命中率不会低于99%，如果低于这个值的话就要考虑加大innodb buffer pool。
> 2.Innodb_buffer_pool_pages_total参数表示缓存页面的总数量（一页16k，所以总共8M），Innodb_buffer_pool_pages_data代表有数据的缓存页数，Innodb_buffer_pool_pages_free代表没有使用的缓存页数。如果Innodb_buffer_pool_pages_free偏大的话，证明有很多缓存没有被利用到，这时可以考虑减小缓存，相反Innodb_buffer_pool_pages_data过大就考虑增大缓存
> 实际看到第一个比例大约是85%，第二个也是free数目为415，data数目为7762，一个134M，所以需要扩这个值。扩size到1G后，命中率大概到了99%，查询速率提高了一倍，原来1s的接口，现在只需要500ms。 

* 查看引擎状态
> show engine innodb status; 

* 查看IP占用
> select substring_index(host,':' ,1) as client_ip,Command,Time from information_schema.processlist; 

虽然数据库有外部机制解除死锁，但是也有持有锁事务拿这锁一天不释放，别人都等着的情况。

参考文档：  
https://www.cnblogs.com/cnsre/p/13298230.html
https://weikeqin.com/2019/09/05/mysql-lock-table-solution/

## 慢SQL导致CPU升高
这个需要注意的一点是，当CPU 100%的问题已经发生后，真实的慢查询和因为CPU 100%导致的被影响的普通查询会混到一起，故难以直接从processlist和slowlog来查看元凶，需要一些特征去甄别。  
那么为什么慢SQL会导致CPU升高呢？  
结合之前的MYSQL线程模型，当大量的慢SQL并发出现时，数据库建立大量线程，而每个线程又无法在一定时间内完成任务，于是导致上下文切换。  
导致原来单线程请求1s能返回的结果，在大并发的情况下会很长时间无法返回，超过40s。  
并且因为这些操作挤占了CPU，导致MYSQL对其他请求的处理也慢了，最后前端所有请求都报请求超时。  

按上面给的查CPU高的SQL命令，查出来占CPU很多的线程是很简单的单表SQL。最后解决是去修改/etc/my.cnf配置，调优数据库配置。


参考文档：
https://cloud.baidu.com/doc/RDS/s/zjwvz108y
https://help.aliyun.com/document_detail/187559.html
https://www.modb.pro/db/73685
https://database.51cto.com/art/202008/624316.htm

PS: 逻辑读与物理读  
当数据库执行业务查询、修改语句时，CPU会先从内存中请求数据库（默认一页 8kb）。
* 逻辑读，如果内存中存在对应数据，CPU执行计算任务后会将结果返回给用户，可能涉及到排序类高消耗CPU的动作。
* 物理读，如果内存中不存在数据，则在上面基础上增加从磁盘获取数据的动作。  
性能较低的SQL，查询数据过多，则会涉及过多逻辑读，导致CPU利用率过高。并且也可能导致数据库产生大量物理读，导致IOPS、IO时延过高。  

参考文档：  
https://cloud.tencent.com/document/product/1130/
http://blog.itpub.net/26736162/viewspace-2689186/


## 计算量大导致CPU升高
因为数据量比较大，即使索引没什么问题，执行计划也 OK，也会导致 CPU 100%，而且结合 MySQL one-thread-per-connection 的特性，并不需要太多的并发就能把 CPU 使用率跑满。  
这一类查询其实是是比较好查的，因为执行时间一般会比较久，在 processlist 里面就会非常显眼，反而是 slowlog 里面可能找不到，因为没有执行完的语句是不会记录的。  
这一类问题一般来说有三种比较常规的解决方案：
读写分离，把这一类查询放到平时业务不怎么用的只读从库去。
在程序段拆分 SQL，把单个大查询拆分成多个小查询。
使用 HBASE，Spark 等 OLAP 的方案来支持。

当然如果仅是高QPS，那就是要去堆资源或者分库分表了。

PS:有关MySQL one-thread-per-connection 即每一个客户端请求，MYSQL服务器都会给该客户端创建一个线程，连接数越多则线程数越多。  
如果大部分线程处于空闲状态，则不会对服务器的性能造成很大的影响。但如果同时执行的线程太多，会导致操作系统频繁的上下文切换。
引入线程池的目的，就是为了减少同时运行的线程的数量，降低上下文切换的次数。  

目前MySQL线程池只在Percona，MariaDB，Oracle MySQL企业版中提供。Oracle MySQL社区版并不提供。

增加解释：
活跃线程高一定会带来CPU使用率的增长。抽象来说，MySQL实现中，每一个CPU只能在同一时间内处理一个请求。假设是16C规格的集群，最多只能同时处理16个请求。但是要注意，这里的请求指的是内核层面，而非应用的并发层面。

如果排除掉慢查询导致的请求无法正常处理，活跃线程堆积一般都是由于现网业务流量增长造成的。通过查看性能曲线，如果整体流量以及请求趋势和活跃线程的堆积趋势一致，那么说明集群资源已经达到上限，此时需要通过对数据库集群增加只读节点或者扩容集群规格来解决。

需要注意的是，在活跃线程达到临界点时，可能在CPU层面开始产生争抢，内核中会产生大量的mutex排他锁，此时性能曲线表现特征为高CPU使用率、高活跃线程、低IO或低QPS。另外一种情况是突然的业务洪峰，建立连接速度非常快，也可能在CPU层面产生大量争抢，从而导致请求堆积。此类问题一般可以通过开启集群的thread_pool特性进行流控缓解，具体请参见Thread Pool。如果活跃线程有所缓解，同时还要注意应用侧是否已经产生了业务堆积，如果CPU负载较高同时活跃线程依然高居不下，此时则同样要考虑是不是对集群进行扩容操作。

另有一种情况是前端连接风暴导致集群流量瞬间堆积，此时流量属于异常流量，一般出现在流量数据被爬虫拉取数据的场景下。此时可以通过SQL限流的方式进行请求拒绝，具体操作请参见会话管理。

参考文档：  
https://www.alibabacloud.com/help/zh/doc-detail/294514.htm
http://mysql.taobao.org/monthly/2016/02/09/
https://www.jianshu.com/p/6c6073aa5331


## 数据量打满磁盘使用
```
java.sql.SQLException： The table '***' is full
```
磁盘满了 上去df -h 看50G根目录只剩20k了。查/etc/my.cnf看mysql运行目录，/var/lib/mysql
进入目录用du -sh *,看是哪个数据库的哪个表占用过多。
定位到Event表占用最大，1500W条数据占用了19G；然后是log表，26w数据，占用4.3G(这里是因为之前三个月前的旧数据删除了delete了，但是磁盘空间没释放。实际清理完24w数据只用了400m，清理用时大概9s)。

解决办法临时直接删除部分文件即可。


## delete数据磁盘还在占用

原因:使用delete删除时，mysql并没有把数据文件删除，而是将数据文件的标识位删除，没有整理空间，因此不会释放空间。被删除文件会被放到一个链接清单中，每当有数据写入，mysql会用这些已删除空间写入。  
因为你如果直接删除了中间的记录，那空间是要整理的（这个操作明显比较耗时），这样直接不整理，后面直接用，提高效率。  

解决方案： 官方推荐使用 OPTIMIZE TABLE命令来优化表，该命令会重新利用未使用的空间，并整理数据文件的碎片。 
查看占用大小
```
SELECT TABLE_NAME, (DATA_LENGTH+INDEX_LENGTH)/1048576, TABLE_ROWS FROM information_schema.tables WHERE TABLE_NAME='kube_eventer';
optimize table kube_eventer
```

注意: 1.optimize执行时会将表锁住，所以不要在高峰期使用。也不要经常使用，每月一次就足够了。


## 数据库连接池大小配置的问题
如果应用与数据库的每次交互都创建线程，完成交互后销毁，则线程创建和销毁的过程会消耗大量资源。所以用一个池子，先预先创建一些线程在里面，需要时去池子里拿，以此减少频繁创建线程开销。  
这里有两个地方可以配置，MYSQL数据库端有连接池，应用部分也有连接池，下文讨论的是应用的连接池，并且由于压测，只考虑了maxActive的情况。  

假设数据库核数为20核，连接池大小为100。平均每个cpu执行5个线程，这5个线程需要通过时间片轮转去执行，每个程序的执行时间会上升，因为包含了任务执行时间和上下文切换时间，虽然服务器吞吐量上升了。  
而如果连接池大小改成40，则每个cpu轮流执行2个任务，每个任务的平均时间会减少，但是吞吐量会下降。  

于是这就是一个吞吐量和执行时间的取舍问题，并且需要具体应用具体对待， 压测CPU使用量在合理范围内应尽量提高吞吐量，但吞吐量的同时可能会导致qps的延长。  
并且如果数据库连接池过大，会导致查询交易进入应用后，全部压向数据库，导致数据库维护线程增多。  
1.如果慢查询或者死锁导致数据库连接长时间无返回，则新的请求来就会报超时未获得数据库连接的错。  
2.压测，数据库连接池大小为100，应用容器cpu使用率为2%，数据库cpu使用率为85%；数据库连接池大小为40，应用容器cpu使用率为7%，数据库cpu使用率为50%左右。

参考文档：  
https://blog.csdn.net/Let_me_tell_you/article/details/102877921  
https://www.cnblogs.com/rickiyang/p/12239907.html

## MYSQL OOM问题
MySQL也会OOM，可能是系统软件OOM，也可能是某条SQL OOM。

参考文档：  
https://cloud.tencent.com/document/product/236/32534

## 死锁 org.springframework.dao.DeadlockLoserDataAccessException问题
```
org.springframework.dao.DeadlockLoserDataAccessException:
### Error updating database.  Cause: com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
### The error may involve com.x.x.dao.xdao.update-Inline
### The error occurred while setting parameters
### SQL: insert into ***  DUPLICATE KEY
### Cause: com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
com.mysql.jdbc.exceptions.jdbc4..MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction
```

INSERT ON DUPLICATE KEY在执行时，innodb引擎会先判断插入的行是否产生重复key错误

如果存在，在对该现有的行加上S（共享锁）锁，返回该行数据给mysql,然后mysql执行完duplicate后的update操作，然后对该记录加上X（排他锁），最后进行update写入

如果有两个事务并发的执行同样的语句，那么就会产生death lock。而我们正好就是两个后端跑程序。+

解决方法：

1、尽量不对存在多个组合唯一键的table上使用该语句

2、在有可能有并发事务执行的insert 的内容一样情况下不使用该语句。程序先用select查看是否存在，再决定是insert还是update，但是这样耗时比较多


## 查询 org.springframework.jdbc.UncategorizedSQLException:
```
org.springframework.jdbc.UncategorizedSQLException:

```
select count(1) 触发

可能原因：
1.和字符集相关。 比如varchar的字符集从asci改到utf-8
2.还是和字符集相关，同样的utf-8，如果两个表的字段做连表查询，但是字符集排序一个是utf8_unicode_ci,另一个是utf8_general_ci,就会报错。


## 未获取到锁 org.springframework.dao.CannotAcquireLockException:
```
org.springframework.dao.CannotAcquireLockException
org.springframework.dao.CannotAcquireLockException: PreparedStatementCallback; 
SQL [insert into pla_common(common_key,common_value,stint,url) values(?,?,?,?)]; 
Lock wait timeout exceeded; try restarting transaction; 
nested exception com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException:
 Lock wait timeout exceeded; try restarting transaction
```
delete触发  由于该操作会等待超时时间，故会在SQL这等很久，程序才会继续执行。

大多数都是事务嵌套，可能原因：
1.代码中一个service包含两次insert。导致在一个事务中，对同一张表进行了两次insert。第一个未提交，也就未释放锁，第二个insert等待锁超时，就抛出了CannotAcquireLockException异常。
2.service里面调用service，且两个service都配置了事务。顶层service名称为A，内层service名称为B。解决方法：内层service实现方法配置@Transactional(propagation=Propagation.SUPPORTS)
3.数据库压力太大，数据被锁定，导致事务出现问题，需要优化慢SQL。数据库中该表是否有事务抢占了该锁，导致该请求无法获取锁，然后一直等待，等待时间超过数据库设置的默认时间就会失败。
4.A事务持有锁 但未提交。B事务需要锁 但拿不到。

参考文档：  
https://juejin.cn/post/6917605106582028302
https://wsa.jianshu.io/p/bc97a6b2f66d
https://www.jianshu.com/p/245cab135f77


## 连接池 org.springframework.transaction.CannotCreateTransactionException:
```
org.springframework.transaction.CannotCreateTransactionException:

```
druid连接池报60s超时 active 100，maxActive 100，runSQLCount 26
连接池资源用尽。

可能原因： 
1.同时有几十个插入语句事务一直没有结束（事务始终处于开启状态，没有提交），数据库事务超时会被数据库关闭。数据库连接被数据库端强制关闭了，但是druid连接池不知道，所以后来那些阻塞事务都被强制终结后依然获取不到连接！  
解决办法就是增加默认socket连接然后定时清理无效的连接 。
2.和Druid逻辑有关 可能高版本解决 。  

参考文档： 
https://github.com/alibaba/druid/issues/4288
https://github.com/alibaba/druid/issues/2130
https://www.jianshu.com/p/360aa37ac97a
https://www.cxyzjd.com/article/xiaoanzi123/100147072
https://www.shuzhiduo.com/A/kjdwBbQA5N/


## 类型不匹配 org.mybaits.spring.MyBatisSystemException:
```
org.mybaits.spring.MyBatisSystemException:

```
update触发 data too long触发
一般都是插入类型不匹配、SQL语句条件值有问题。


## org.springframework.dao.DataIntegrityViolationException:
```
org.springframework.dao.DataIntegrityViolationException:

```
可能原因: 
1.插入或更新数据时违反了完整性，如违反了唯一性限制。比如主键冲突。
2.传入regDate这个属性的数据是空字符[""],而数据库对应的类型是datetime,所以发生了异常。
3.插入的值比字段规定的长度18要长，所以导致出错了。
4.数据库有必填字段，而你传入的数据中没有

## org.springframework.dao.RecoverableDataAccessException:
```
org.springframework.dao.RecoverableDataAccessException:

```
insert触发 有link failure的报错
可能原因：
1.mysql连接池中的连接已经expired的问题。最终通过在MYSQL的JDBC连接串上加了autoReconnectForPools=true解决。
2.mysql默认如果8小时没有请求的话就会断开链接。show global variables like 'wait_timeout'; 