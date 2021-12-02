
Spring 3.1之后，引入了注解缓存技术，其本质上不是一个具体的缓存实现方案，而是一个对缓存使用的抽象，通过在既有代码中添加少量自定义的各种annotation，即能够达到使用缓存对象和缓存方法的返回对象的效果。Spring的缓存技术具备相当的灵活性，不仅能够使用SpEL（Spring Expression Language）来定义缓存的key和各种condition，还提供开箱即用的缓存临时存储方案，也支持和主流的专业缓存集成。其特点总结如下：

少量的配置annotation注释即可使得既有代码支持缓存；
支持开箱即用，不用安装和部署额外的第三方组件即可使用缓存；
支持Spring Express Language（SpEL），能使用对象的任何属性或者方法来定义缓存的key和使用规则条件；
支持自定义key和自定义缓存管理者，具有相当的灵活性和可扩展性。
和Spring的事务管理类似，Spring Cache的关键原理就是Spring AOP，通过Spring AOP实现了在方法调用前、调用后获取方法的入参和返回值，进而实现了缓存的逻辑。而Spring Cache利用了Spring AOP的动态代理技术，即当客户端尝试调用pojo的foo()方法的时候，给它的不是pojo自身的引用，而是一个动态生成的代理类。

Spring Cache主要使用三个注释标签，即@Cacheable、@CachePut和@CacheEvict，主要针对方法上注解使用，部分场景也可以直接类上注解使用，当在类上使用时，该类所有方法都将受影响。
而既然已经可以使用了注解，那我们也可以使用自定义注解来封装更多Cache操作。  

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/spring-cache.JPG)


参考文档：  
https://tech.meituan.com/2017/03/17/cache-about.html

## 实际使用
在某个Service层的方法前加上@Cacheable注解,注明每次去更新Redis的什么值。
```text
    @Override
    @Cacheable(value = REDISKEY)
    public List<Object> findAll() {
        return XXX;
    }
```