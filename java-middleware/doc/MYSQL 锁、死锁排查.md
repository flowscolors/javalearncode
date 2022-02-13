
数据库中的锁，也分很多类型。并且不同存储引擎的锁也不同，只是一般我们都是基于Innodb展开讨论。
(●'◡'●)


## 2.MYSQL InnoDB 中的各种锁 
逻辑上的锁：
* 乐观锁和悲观锁   
确保在多个事务同时存取数据库中同一数据时不破坏事务的隔离性和统一性以及数据库的统一性，乐观锁和悲观锁是并发控制主要采用的技术手段。

悲观锁 - 假定会发生并发冲突，屏蔽一切可能违反数据完整性的操作。在查询完数据的时候就把事务锁起来，直到提交事务（COMMIT）。实现方式：使用数据库中的锁机制。

乐观锁 - 假设不会发生并发冲突，只在提交操作时检查是否违反数据完整性。在修改数据的时候把事务锁起来，通过 version 的方式来进行锁定。实现方式：使用 version 版本或者时间戳

* MVCC 乐观锁的一种实现
多版本并发控制（Multi-Version Concurrency Control, MVCC）是 InnoDB 存储引擎实现隔离级别的一种具体方式，用于实现提交读和可重复读这两种隔离级别。而未提交读隔离级别总是读取最新的数据行，要求很低，无需使用 MVCC。可串行化隔离级别需要对所有读取的行都加锁，单纯使用 MVCC 无法实现。

MVCC 的思想是：

保存数据在某个时间点的快照。写操作（DELETE、INSERT、UPDATE）更新最新的版本快照，而读操作去读旧版本快照，没有互斥关系，这一点和 CopyOnWrite 类似。
脏读和不可重复读最根本的原因是事务读取到其它事务未提交的修改。在事务进行读取操作时，为了解决脏读和不可重复读问题，MVCC 规定只能读取已经提交的快照。当然一个事务可以读取自身未提交的快照，这不算是脏读。

实际意义上的锁：
1.Shared and Exclusive Locks 共享锁/读写锁
2.Intention Locks            意向锁，并不会阻塞任何请求，只是用来表明自己的意图，InnoDB会自动加。
3.Record Locks               记录锁，行锁，select ··· for update 锁的是索引中的记录。
4.Gap Locks                  间隙锁，select···for update中where是范围时会使用，锁的是索引中的范围记录。
5.Next-Key Locks             行锁与gap锁的一个组合锁，。在默认的REPEATABLE READ隔离级别下，InnoDB会使用next-key lock来作为默认的锁，并在gap lock部分提到的特定条件下，放开gap lock的限制从而退化为单纯的行锁。
6.Insert Intention Locks
7.AUTO-INC Locks
8.Predicate Locks for Spatial Indexes


* 行级锁和表级锁
从数据库的锁粒度来看，MySQL 中提供了两种封锁粒度：行级锁和表级锁。

表级锁（table lock） - 锁定整张表。用户对表进行写操作前，需要先获得写锁，这会阻塞其他用户对该表的所有读写操作。只有没有写锁时，其他用户才能获得读锁，读锁之间不会相互阻塞。
                    表锁有表共享锁（读锁）、表独占锁（写锁）。使用命令可以添加。
                    
行级锁（row lock） - 仅对指定的行记录进行加锁，这样其它进程还是可以对同一个表中的其它记录进行操作。
                    行锁有共享锁、排他锁两种模式。一般行锁主要加在索引上，而非记录。但是要注意对非索引字段进行更新时，会从行锁升级为表锁。
                    “For Update” 会通知数据库对所有返回结果行自动加锁，加的是排他锁。也可以在select的最后加“lock in share mode”，可添加共享锁。
                    
* 读写锁/共享锁与排他锁 
独享锁（Exclusive），简写为 X 锁，又称写锁。使用方式：SELECT ... FOR UPDATE;
共享锁（Shared），简写为 S 锁，又称读锁。使用方式：SELECT ... LOCK IN SHARE MODE;
写锁和读锁的关系，简言之：独享锁存在，其他事务就不能做任何操作。

当多个用户并发地存取数据时，在数据库中就会产生多个事务同时存取同一数据的情况。当两个事务需要一组有冲突的锁，而不能将事务继续下去的话，就会出现死锁，
在数据库中有两种基本的锁类型：排它锁(Exclusive Locks，即X锁)和共享锁(Share Locks，即S锁)。当数据对象被加上排它锁时，其他的事务不能对它读取和修改。加了共享锁的数据对象可以被其他事务读取，但不能修改。数据库利用这两 种基本的锁类型来对数据库的事务进行并发控制。

InnoDB 下的行锁、间隙锁、next-key 锁统统属于独享锁。

*  意向锁

意向锁的作用是：当存在表级锁和行级锁的情况下，必须先申请意向锁（表级锁，但不是真的加锁），再获取行级锁。使用意向锁（Intention Locks）可以更容易地支持多粒度封锁。

意向锁是 InnoDB 自动加的，不需要用户干预。


* Next-key 锁
Next-Key 锁是 MySQL 的 InnoDB 存储引擎的一种锁实现。

MVCC 不能解决幻读问题，Next-Key 锁就是为了解决幻读问题。在可重复读（REPEATABLE READ）隔离级别下，使用 MVCC + Next-Key 锁 可以解决幻读问题。

另外，根据针对 SQL 语句检索条件的不同，加锁又有以下三种情形需要我们掌握。

Record Lock - 行锁对索引项加锁，若没有索引则使用表锁。
Gap Lock - 对索引项之间的间隙加锁。锁定索引之间的间隙，但是不包含索引本身。例如当一个事务执行以下语句，其它事务就不能在 t.c 中插入 15。SELECT c FROM t WHERE c BETWEEN 10 and 20 FOR UPDATE;
Next-key lock -它是 Record Lock 和 Gap Lock 的结合，不仅锁定一个记录上的索引，也锁定索引之间的间隙。它锁定一个前开后闭区间。
索引分为主键索引和非主键索引两种，如果一条 SQL 语句操作了主键索引，MySQL 就会锁定这条主键索引；如果一条语句操作了非主键索引，MySQL 会先锁定该非主键索引，再锁定相关的主键索引。在 UPDATE、DELETE 操作时，MySQL 不仅锁定 WHERE 条件扫描过的所有索引记录，而且会锁定相邻的键值，即所谓的 next-key lock。

当两个事务同时执行，一个锁住了主键索引，在等待其他相关索引。另一个锁定了非主键索引，在等待主键索引。这样就会发生死锁。发生死锁后，InnoDB 一般都可以检测到，并使一个事务释放锁回退，另一个获取锁完成事务。


锁的实验：  
https://weikeqin.com/2019/09/05/mysql-lock-table-solution/




## 锁检查
> select * from performance_schema.events_statements_current WHERE THREAD_ID = 105;

使用上述命令可以看到SQL命令与TIMER_START、TIMER_END、TIMER_WAIT 、LOCK_TIME四个字段，单位都是ps（皮秒，亿万分之一秒）



## 死锁案例
InnoDB存储引擎使用一种叫做等待图（wait-for graph）的方式来自动检测死锁。如果发现死锁，则会回滚一个事务。
写一个死锁案例:
```TEXT
create database test;

use test;

create table account (
id int not null auto_increment,
name varchar(30) not null default '',
balance int not null default 0,
primary key(id)
) engine=InnoDB default charset=utf8mb4;

insert into test.account(name,balance) values ('张三','300'),('李四','400');
```

第一步，打开终端A，将事务等级设置为可重复读，开启事务后为account中id为1的数据添加排他锁。
set session transaction isolation level repeatable read ; 

start transaction;

select * from account where id = 1 for update;

第二步，打开终端B，将事务等级设置为可重复读，开启事务后为account中id为2的数据添加排他锁。
set session transaction isolation level repeatable read ; 

start transaction;

select * from account where id = 2 for update;

第三步，回到终端A，对account中id为2的数据加锁，此时线程会一直卡住，因为在等待终端B中事务的锁释放
select * from account where id = 2 for update;

第三步，回到终端B，对account中id为1的数据加锁，此时发生了死锁，可以通过 show engine innodb status; 查看死锁日志。
select * from account where id = 1 for update;

```TEXT

=====================================
2022-01-16 09:52:58 0x7f77700cf700 INNODB MONITOR OUTPUT
=====================================
Per second averages calculated from the last 41 seconds
-----------------
BACKGROUND THREAD
-----------------
srv_master_thread loops: 16 srv_active, 0 srv_shutdown, 467132 srv_idle
srv_master_thread log flush and writes: 467148
----------
SEMAPHORES
----------
OS WAIT ARRAY INFO: reservation count 22
OS WAIT ARRAY INFO: signal count 22
RW-shared spins 0, rounds 42, OS waits 21
RW-excl spins 0, rounds 0, OS waits 0
RW-sx spins 0, rounds 0, OS waits 0
Spin rounds per wait: 42.00 RW-shared, 0.00 RW-excl, 0.00 RW-sx
------------------------
LATEST DETECTED DEADLOCK
------------------------
2022-01-16 09:51:31 0x7f777008d700
*** (1) TRANSACTION:
TRANSACTION 9011, ACTIVE 56 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 3 lock struct(s), heap size 1136, 2 row lock(s)
MySQL thread id 7, OS thread handle 140150957192960, query id 236 192.168.122.1 root statistics
select * from account where id = 2 for update
*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 65 page no 3 n bits 72 index PRIMARY of table `test`.`account` trx id 9011 lock_mode X locks rec but not gap waiting
Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000002; asc     ;;
 1: len 6; hex 00000000232e; asc     #.;;
 2: len 7; hex a80000011c011c; asc        ;;
 3: len 6; hex e69d8ee59b9b; asc       ;;
 4: len 4; hex 80000190; asc     ;;

*** (2) TRANSACTION:
TRANSACTION 9012, ACTIVE 25 sec starting index read
mysql tables in use 1, locked 1
3 lock struct(s), heap size 1136, 2 row lock(s)
MySQL thread id 8, OS thread handle 140150957463296, query id 240 192.168.122.1 root statistics
select * from account where id = 1 for update
*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 65 page no 3 n bits 72 index PRIMARY of table `test`.`account` trx id 9012 lock_mode X locks rec but not gap
Record lock, heap no 3 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000002; asc     ;;
 1: len 6; hex 00000000232e; asc     #.;;
 2: len 7; hex a80000011c011c; asc        ;;
 3: len 6; hex e69d8ee59b9b; asc       ;;
 4: len 4; hex 80000190; asc     ;;

*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 65 page no 3 n bits 72 index PRIMARY of table `test`.`account` trx id 9012 lock_mode X locks rec but not gap waiting
Record lock, heap no 2 PHYSICAL RECORD: n_fields 5; compact format; info bits 0
 0: len 4; hex 80000001; asc     ;;
 1: len 6; hex 00000000232e; asc     #.;;
 2: len 7; hex a80000011c0110; asc        ;;
 3: len 6; hex e5bca0e4b889; asc       ;;
 4: len 4; hex 8000012c; asc    ,;;

*** WE ROLL BACK TRANSACTION (2)
------------
TRANSACTIONS
------------
Trx id counter 9013
Purge done for trx's n:o < 9011 undo n:o < 0 state: running but idle
History list length 0
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 421625986196008, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421625986194168, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421625986193248, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 9011, ACTIVE 143 sec
3 lock struct(s), heap size 1136, 2 row lock(s)
MySQL thread id 7, OS thread handle 140150957192960, query id 247 192.168.122.1 root
--------
FILE I/O
--------
I/O thread 0 state: waiting for completed aio requests (insert buffer thread)
I/O thread 1 state: waiting for completed aio requests (log thread)
I/O thread 2 state: waiting for completed aio requests (read thread)
I/O thread 3 state: waiting for completed aio requests (read thread)
I/O thread 4 state: waiting for completed aio requests (read thread)
I/O thread 5 state: waiting for completed aio requests (read thread)
I/O thread 6 state: waiting for completed aio requests (write thread)
I/O thread 7 state: waiting for completed aio requests (write thread)
I/O thread 8 state: waiting for completed aio requests (write thread)
I/O thread 9 state: waiting for completed aio requests (write thread)
Pending normal aio reads: [0, 0, 0, 0] , aio writes: [0, 0, 0, 0] ,
 ibuf aio reads:, log i/o's:, sync i/o's:
Pending flushes (fsync) log: 0; buffer pool: 0
692 OS file reads, 280 OS file writes, 123 OS fsyncs
0.00 reads/s, 0 avg bytes/read, 0.00 writes/s, 0.00 fsyncs/s
-------------------------------------
INSERT BUFFER AND ADAPTIVE HASH INDEX
-------------------------------------
Ibuf: size 1, free list len 0, seg size 2, 0 merges
merged operations:
 insert 0, delete mark 0, delete 0
discarded operations:
 insert 0, delete mark 0, delete 0
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
Hash table size 34679, node heap has 0 buffer(s)
0.00 hash searches/s, 0.00 non-hash searches/s
---
LOG
---
Log sequence number 13593503
Log flushed up to   13593503
Pages flushed up to 13593503
Last checkpoint at  13593494
0 pending log flushes, 0 pending chkp writes
86 log i/o's done, 0.00 log i/o's/second
----------------------
BUFFER POOL AND MEMORY
----------------------
Total large memory allocated 137428992
Dictionary memory allocated 114709
Buffer pool size   8192
Free buffers       7627
Database pages     565
Old database pages 219
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 0, not young 0
0.00 youngs/s, 0.00 non-youngs/s
Pages read 504, created 61, written 174
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 565, unzip_LRU len: 0
I/O sum[0]:cur[0], unzip sum[0]:cur[0]
--------------
ROW OPERATIONS
--------------
0 queries inside InnoDB, 0 queries in queue
0 read views open inside InnoDB
Process ID=1, Main thread ID=140150641121024, state: sleeping
Number of rows inserted 135, updated 0, deleted 0, read 130
0.00 inserts/s, 0.00 updates/s, 0.00 deletes/s, 0.00 reads/s
----------------------------
END OF INNODB MONITOR OUTPUT
============================

```

参考文档：  
https://www.cnblogs.com/sessionbest/articles/8689082.html
https://cloud.tencent.com/document/product/1130/42596
https://blog.csdn.net/weixin_39845406/article/details/113144655

