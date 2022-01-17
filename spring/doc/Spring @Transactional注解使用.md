
事务（Transaction）是面向关系型数据库（RDBMS）企业应用程序的重要组成部分，用来确保数据的完整性和一致性。

事务具有以下 4 个特性，即原子性、一致性、隔离性和持久性，这 4 个属性称为 ACID 特性。
原子性（Atomicity）：一个事务是一个不可分割的工作单位，事务中包括的动作要么都做要么都不做。
一致性（Consistency）：事务必须保证数据库从一个一致性状态变到另一个一致性状态，一致性和原子性是密切相关的。
隔离性（Isolation）：一个事务的执行不能被其它事务干扰，即一个事务内部的操作及使用的数据对并发的其它事务是隔离的，并发执行的各个事务之间不能互相打扰。
持久性（Durability）：持久性也称为永久性，指一个事务一旦提交，它对数据库中数据的改变就是永久性的，后面的其它操作和故障都不应该对其有任何影响。

## 1.Spring事务
Spring的事务其实是依赖MySQL事务的拓展，其实是基于Commit的使用。如果数据库不支持事务则无法生效。

如果没有Spring，我们也可以使用JDBC来操作事务。实例如下，我们需要手动开启、关闭事务。而如果使用Spring的事务则不必手动操作，可以自动开始、提交、回滚事务。
```shell script
Class.forName("com.mysql.jdbc.Driver");
Connection conn = DriverManager.getConnection(url,"root","passowrd");
conn.setAutoCommit(true/false);
PrearedStatment ps = conn.prepareStatement(sql);
conn.commmit();
conn.rollback();
ps.close();
conn.close();
```

Java的事务管理有 2 种方式：
* 传统的编程式事务管理，即通过编写代码实现的事务管理；灵活性高，但难以维护。如在Hibernate中可以显式调用 beginTransaction()、commit()、rollback() 等事务管理相关的方法。而Spring也提供了对于API，见下一Part。
* 基于Spring AOP 技术实现的声明式事务管理。其最大的优点在于无须通过编程的方式管理事务，只需要在配置文件中进行相关的规则声明，就可以将事务规则应用到业务逻辑中。原理也很简单，使用AOP对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。一般有两种使用方式:  
** 基于 XML 方式的声明式事务管理。
** 通过 Annotation 注解方式的事务管理 @Transactional及相关参数。

Spring框架在启动时扫描XML或@Transaction注解修饰的类，为这些方法生成代理对象，并进行相关参数注入，从而在代理对象中开启、提交、回滚事务。

使用Spring管理事务，可以指定在方法抛出异常时，哪些方法能回滚异常，哪些方法不回滚异常都可以自己定义。
默认情况下，会在方法抛出RuntimeException时回滚事务。

## Spring 事务三大接口
>事务管理接口 PlatformTransactionManager、TransactionDefinition 和 TransactionStatus 是事务的 3 个核心接口。

* PlatformTransactionManager 可获取事务状态，并实际进行事务的提交（commit）、回滚（rollback）  

Spring并不是直接管理事务，而是提供了多种事务管理器，相当于把具体实现交给了Hibernate、Mybatis、JTAd等持久层框架实现，自己仅提供接口。
主要三个方法 getTranscation() 、commit() 、rollback() ,所以当我们想自定义一个对某个HTTP访问的事务操作，可以自己定义一个事务管理器，来完成对某个接口的回滚操作。

* TransactionDefinition  可获取事务名称、隔离级别、传播行为、超时时间、是否只读。 
 
主要定义了事务的相关方法，传播类型、事务属性等常量。

* TransactionStatus 可获取事务的状态、是否存在保存点、是否完成、是否获取新事务。

TransactionStatus主要用来存储事务执行的状态，并且定义了一组方法，用来判断或读取事务的状态信息。

参考文档：  
https://www.codehome.vip/archives/spring-transaction

## Spring事务的传播特性
1、 PROPAGATION_REQUIRED: 如果存在一个事务，则支持当前事务。如果没有事务则开启
2、 PROPAGATION_SUPPORTS: 如果存在一个事务，支持当前事务。如果没有事务，则非事务的执行
3、PROPAGATION_MANDATORY: 如果已经存在一个事务，支持当前事务。如果没有一个活动的事务，则抛出异常。
4、 PROPAGATION_REQUIRES_NEW: 总是开启一个新的事务。如果一个事务已经存在，则将这个存在的事务挂起。
5、PROPAGATION_NOT_SUPPORTED: 总是非事务地执行，并挂起任何存在的事务。
6、 PROPAGATION_NEVER: 总是非事务地执行，如果存在一个活动事务，则抛出异常
7、 PROPAGATION_NESTED：如果一个活动的事务存在，则运行在一个嵌套的事务中. 如果没有活动事务。

以上会在程序执行过程中创建新事务的传播类型有PROPAGATION_REQUIRED、PROPAGATION_REQUIRES_NEW、PROPAGATION_NESTED。

一般常用的三种传播模式：
PROPAGATION_REQUIRED 默认参数，没有则开启。

PROPAGATION_NOT_SUPPORTED 总是非事务的执行，适用于站内信、网页提示信息、短信、邮件等，哪怕业务抛出失败，也要继续执行的逻辑。

PROPAGATION_REQUIRES_NEW  总是创建新的事物执行，使用于不受外层方法影响的场景。如记录审计日志的操作，不能因为主流程插入失败，则审计日志表就不插入。

## 3.Spring中事务需要注意的一些坑
事务失效的几种场景：
1.数据库不支持事务，比如MyISAM的MYSQL。
2.声明注解的类没有注入到Spring中，比如没有写@Service。 @Transaction依赖Spring IOC、AOP。
3.声明注解的方法不是public。  注解底层实现依赖Spring AOP，需要获得公共方法。
4.调用同一个类中的方法，AB上面都使用了事务注解，A调用B，则B的事务失败。
5.方法的传播类型不支持事务，A方法中调用B的方法，B的事务传播类型是NOT SUPPORT,故B的事务不会执行。
6.不正确的捕获异常，由于事务是基于异常触发的，如果直接catch了则触发不到事务。



## 4.因事务导致的生产事故
现象：应用发现在使用一段时间后发生SQL语句无法执行的问题，导致前端无法获得数据。重启后，使用一段时间后仍会出现该问题。
定位：最后定位到是某个批量功能，没有使用mybatis来操作数据库，而是直接使用jdbcTemplate操作数据库，由于手段开关事务不当导致的数据库连接未释放。
分析：相关逻辑是使用DruidDataSource获取数据源，搭配jdbcTemplate操作数据库，使用 TransactionSynchroniaztionManger（事务同步管理器）和DataSourceUtil 手动操作事务。
      即获取数据库连接 -> 设置手动提交 -> 遇到异常回滚 这里首先代码没做connection.close() 因为当时认为druid会自动回收，当然实际并不是这个问题导致的，加了还会触发。
      --
      具体源码可以看TransactionSynchroniaztionManger 中的init、active、bingResource() 方法 和 DataSourceUtils的getConnection()方法、releaseConnection()方法。
      简答的来就是使用自定义的getConnection()方法可以获得到连接，并激活事务。然后使用jdbcTempalete使用DataSourceUtils获取连接，之后使用execute()方法执行对数据库操作。
      需要注意的是每个execute()方法执行前都会调用DataSOurce.getConnection() 而这就是问题关键。 分析代码流程···
      哪怕再finally代码块中调用connection.close()关闭连接，但是未调用releaseConnectin()方法将计数器置0，导致无法释放连接。当下一个操作来又会重复该逻辑，最后占满数据库连接。
      jdbcTempalete中的每个execute()方法都调用了releaseConnectin()，而我们自己代码没有调用，导致最后异常。
