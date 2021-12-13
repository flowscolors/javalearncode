
事务（Transaction）是面向关系型数据库（RDBMS）企业应用程序的重要组成部分，用来确保数据的完整性和一致性。

事务具有以下 4 个特性，即原子性、一致性、隔离性和持久性，这 4 个属性称为 ACID 特性。
原子性（Atomicity）：一个事务是一个不可分割的工作单位，事务中包括的动作要么都做要么都不做。
一致性（Consistency）：事务必须保证数据库从一个一致性状态变到另一个一致性状态，一致性和原子性是密切相关的。
隔离性（Isolation）：一个事务的执行不能被其它事务干扰，即一个事务内部的操作及使用的数据对并发的其它事务是隔离的，并发执行的各个事务之间不能互相打扰。
持久性（Durability）：持久性也称为永久性，指一个事务一旦提交，它对数据库中数据的改变就是永久性的，后面的其它操作和故障都不应该对其有任何影响。

## 1.Spring事务
Spring 的事务管理有 2 种方式：
* 传统的编程式事务管理，即通过编写代码实现的事务管理；灵活性高，但难以维护。如在Hibernate中可以显式调用 beginTransaction()、commit()、rollback() 等事务管理相关的方法。而Spring也提供了对于API，见下一Part。
* 基于 AOP 技术实现的声明式事务管理。其最大的优点在于无须通过编程的方式管理事务，只需要在配置文件中进行相关的规则声明，就可以将事务规则应用到业务逻辑中。原理也很简单，使用AOP对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。一般有两种使用方式:  
** 基于 XML 方式的声明式事务管理。
** 通过 Annotation 注解方式的事务管理 @Transactional及相关参数。

>事务管理接口 PlatformTransactionManager、TransactionDefinition 和 TransactionStatus 是事务的 3 个核心接口。

PlatformTransactionManager 可获取事务状态，并实际进行事务的提交（commit）、回滚（rollback）  
TransactionDefinition  可获取事务名称、隔离级别、传播行为、超时时间、是否只读。  
TransactionStatus 可获取事务的状态、是否存在保存点、是否完成、是否获取新事务。



参考文档：  
https://www.codehome.vip/archives/spring-transaction

## Spring事务的传播特性
1、 PROPAGATION_REQUIRED: 如果存在一个事务，则支持当前事务。如果没有事务则开启
2、 PROPAGATION_SUPPORTS: 如果存在一个事务，支持当前事务。如果没有事务，则非事务的执行
3、PROPAGATION_MANDATORY: 如果已经存在一个事务，支持当前事务。如果没有一个活动的事务，则抛出异常。
4、 PROPAGATION_REQUIRES_NEW: 总是开启一个新的事务。如果一个事务已经存在，则将这个存在的事务挂起。
5、PROPAGATION_NOT_SUPPORTED: 总是非事务地执行，并挂起任何存在的事务。
6、 PROPAGATION_NEVER: 总是非事务地执行，如果存在一个活动事务，则抛出异常
7、 PROPAGATION_NESTED：如果一个活动的事务存在，则运行在一个嵌套的事务中. 如果没有活动事务,


## 3.Spring中事务需要注意的一些坑



## 4.因事务导致的生产事故