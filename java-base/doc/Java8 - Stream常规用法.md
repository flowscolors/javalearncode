## 1.Stream概览

当我对于一个关系型数据库进行查询时，传入where参数我可以很方便的进行查询。但是当我查Redis中数据，想进行范围查询就很困难。
好在Java8提供的Stream表达式可以某种程度上解决这个问题。把Redis中数据读取到内存进行流式表达式的处理可以得到类似关系型的SQL功能。
```
public Object getOne(String id，String name){
    return selectOne(newLambdaWrapper()
        .eq(Object::getId,id)
        .eq(Object::getName,name)
        .eq(Object::getIsDeleted,0))；
}
```

流式表达式使用一种类似用SQL语句从数据库查询数据的直观方式来提供一种对Java集合运算和表达的高阶抽象。这种风格将要处理的元素集合看成一种流，流在管道中传输，并在管道的节点上进行处理，比如筛选、排序、聚合等。  
```text
+--------------------+       +------+   +------+   +---+   +-------+
| stream of elements +-----> |filter+-> |sorted+-> |map+-> |collect|
+--------------------+       +------+   +------+   +---+   +-------+

List<Integer> transactionsIds = 
             widgets.stream()
             .filter(b -> b.getColor() == RED)
             .sorted((x,y) -> x.getWeight() - y.getWeight())
             .mapToInt(Widget::getWeight)
             .sum();
```

Stream流是一个来自数据源的元素队列并支持聚合操作。有几点需要注意：  
* 元素是特定类型的对象，形成一个队列，Java中的Stream并不存储对象，只是按需计算。
* 数据源 流的来源。 可以是集合，数组，I/O channel， 产生器generator 等。
* 聚合操作 类似SQL语句一样的操作， 比如filter, map, reduce, find, match, sorted等。
* 与之前的Collection操作不同， Stream操作的所有中间操作都是返回流，因此可以实现延迟操作、短路操作。
* 以前对集合遍历都是通过Iterator或者For-Each的方式, 显式的在集合外部进行迭代， 这叫做外部迭代。 Stream提供了内部迭代的方式， 通过访问者模式(Visitor)实现。


## 2.Stream使用
1.获得流。对于集合，在Java8中，集合接口有2个方法来生成流：
* stream() 为集合创建串行流
* parallelStream() 为集合创建并行流
```text
List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl");
List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());
```

2.对流进行操作。
forEach，迭代流中的每个数据
map，映射流中每个元素到对应的结果
filter，通过设置的过滤条件过滤出流中元素
limit，获取指定数目的流
sorted，对流进行排序
collect，把流转换成集合或聚合元素，一般用在最后一个算子
mapToInt、getMax、getMin、getSum、getAverage 直接获得统计计算
reduce，根据计算模型从 stream 中得到一个值。上面的getMax、getMin、getSum都是基于reduce实现。


## 3.Stream实际应用
### 3.1 借用流处理可以很方便对集合交、并集的使用
Java-Java8中实现List交集,并集和差集操作,优点是不需要做一次集合的深拷贝。
 
参考文档：  
https://www.runoob.com/java/java8-streams.html
https://www.chuckfang.com/2020/11/19/1-hour-to-8-secend%EF%BC%81/
http://antsnote.club/2019/04/01/Java-Java8%E4%B8%AD%E5%AE%9E%E7%8E%B0List%E4%BA%A4%E9%9B%86-%E5%B9%B6%E9%9B%86%E5%92%8C%E5%B7%AE%E9%9B%86%E6%93%8D%E4%BD%9C/  
http://antsnote.club/2018/10/18/Java-Java8%E5%87%BD%E6%95%B0%E5%BC%8F%E6%8E%A5%E5%8F%A3/
http://antsnote.club/2018/10/17/Java-Java8%E4%B8%ADStreamAPI%E7%9A%84reduce%E6%96%B9%E6%B3%95%E7%9A%84%E4%BD%BF%E7%94%A8/

### 3.2 Mybatis Plus中使用流处理进行SQL拼接

在我们使用 Mybatis 时会发现，每当要写一个业务逻辑的时候都要在 DAO 层写一个方法，再对应一个 SQL，即使是简单的条件查询、即使仅仅改变了一个条件都要在 DAO层新增一个方法，针对这个问题，Mybatis-Plus 就提供了一个很好的解决方案：lambda 表达式，它可以让我们避免许多重复性的工作。
```text
LambdaQueryWrapper<UserEntity> lqw = Wrappers.lambdaQuery();
lqw.eq(UserEntity::getSex, 0L)
        .like(UserEntity::getUserName, "dun");
List<UserEntity> userList = userMapper.selectList(lqw);
userList.forEach(u -> System.out.println("like全包含关键字查询::" + u.getUserName()));
```

具体使用Mybatis plus的lambda表达式时候，需要注意AbstractWrapper AbstractWrapper条件构造器。相关步骤:  
1.出现的第一个入参 boolean condition 表示该条件是否加入最后生成的 sql 中，例如：query.like(StringUtils.isNotBlank(name), Entity::getName, name) .eq(age!=null && age >= 0, Entity::getAge, age)
2.代码块内的多个方法均为从上往下补全个别 boolean 类型的入参,默认为 true
3.出现的泛型 Param 均为 Wrapper 的子类实例(均具有 AbstractWrapper 的所有方法)
4.方法在入参中出现的 R 为泛型，在普通 wrapper 中是 String ，在 LambdaWrapper 中是函数(例:Entity::getId，Entity 为实体类，getId为字段id的getMethod)
5.方法入参中的 R column 均表示数据库字段，当 R 具体类型为 String 时则为数据库字段名(字段名是数据库关键字的自己用转义符包裹!)!而不是实体类数据字段名!!!，另当 R 具体类型为 SFunction 时项目 runtime 不支持 eclipse 自家的编译器!
6.使用普通 wrapper，入参为 Map 和 List 的均以 json 形式表现!
7.使用中如果入参的 Map 或者 List为空,则不会加入最后生成的 sql 中!
警告:
不支持以及不赞成在 RPC 调用中把 Wrapper 进行传输。
Wrapper 很重 传输 Wrapper 可以类比为你的 controller 用 map 接收值(开发一时爽，维护火葬场) 正确的 RPC 调用姿势是写一个 DTO 进行传输，被调用方再根据 DTO 执行相应的操作 我们拒绝接受任何关于 RPC 传输 Wrapper 报错相关的 issue 甚至 pr。

1.构建查询条件构造器 LambdaQueryWrapper 、更新可以构建LambdaUpdateWrapper  
```text
方式一 使用 QueryWrapper 的成员方法方法 lambda 构建 LambdaQueryWrapper
LambdaQueryWrapper<UserEntity> lambda = new QueryWrapper<UserEntity>().lambda();

方式二 直接 new 出 LambdaQueryWrapper
LambdaQueryWrapper<UserEntity> lambda = new LambdaQueryWrapper<>();

方式三 使用 Wrappers 的静态方法 lambdaQuery 构建 LambdaQueryWrapper 推荐
LambdaQueryWrapper<UserEntity> lambda = Wrappers.lambdaQuery();

方式四：链式查询
List<UserEntity> users = new LambdaQueryChainWrapper<UserEntity>(userMapper)
            .like(User::getName, "雨").ge(User::getAge, 20).list();
```
推荐使用方法三 Wrappers 的静态方法 lambdaQuery 构建 LambdaQueryWrapper 条件构造器。


2.使用查询器AbstractWrapper自己的特殊接口进行SQL查询:
比较值接口 Compare<Children, R>，如 等值 eq、不等于：ne、大于 gt、大于等于：ge、小于 lt、小于等于 le、between、模糊查询：like 等等
嵌套接口 Nested<Param, Children> ，如 and、or
拼接接口 Join<Children>，如 or 、exists
函数接口 Func<Children, R>，如 in 查询、groupby 分组、having、order by排序等

> 注意事项：
> 目前来看Mybatis-Plus lambda 表达式只支持单表操作，因为Mybatis-Plus 并没有提供类似于 join 查询的条件构造器。
> 多表还是要使用Mapper 文件或者基于注解

参考文档：  
https://segmentfault.com/a/1190000039999504