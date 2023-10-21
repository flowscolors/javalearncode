## 1.SPI机制介绍
SPI 全称为 (Service Provider Interface) ，是JDK内置的一种服务提供发现机制。SPI是一种动态替换发现的机制， 比如有个接口，想运行时动态的给它添加实现，你只需要添加一个实现。
我们经常遇到的就是java.sql.Driver接口，其他不同厂商可以针对同一接口做出不同的实现，mysql和postgresql都有不同的实现提供给用户，而Java的SPI机制可以为某个接口寻找服务实现。

相比于IOC，虽然都可以使用配置生成指定的对象，IOC需要代码实现已经在包内，而SPI可以动态调整，把对应路径的jar包换了即可。

其实也很类似策略模式、工厂模式。Java SPI 可以看作是“基于接口的编程＋策略模式＋配置文件”组合实现的动态加载机制。

一般来说接口的定义是双方协调的，有调用方规定接口，提供方实现的，如上面说的SQL驱动接口。也有实现方定义接口，提供给调用方使用的，如某些微服务SDK的接入。


比如:DriverManager是jdbc里管理和注册不同数据库driver的工具类。针对一个数据库，可能会存在着不同的数据库驱动实现。JDBC4.0之后不需要Class.forName来加载驱动，直接获取连接即可，这里使用了Java的SPI扩展机制来实现。

在java中定义了接口java.sql.Driver，并没有具体的实现，具体的实现都是由不同厂商来提供的。而我们使用时使用jdbc:mysql://localhost:3306/test 就能去创建对应mysql的实例。

比如在mysql-connector-java-5.1.45.jar中，META-INF/services目录下会有一个名字为java.sql.Driver的文件：
```text
com.mysql.jdbc.Driver
com.mysql.fabric.jdbc.FabricMySQLDriver
```

而在postgresql-42.2.2.jar中，META-INF/services目录下会有一个名字为java.sql.Driver的文件：
```text
org.postgresql.Driver
```

## 2.SPI机制实现
1.双方约定接口实现，提供方实现完成，在提供方jar包classpath下的META-INF/services/目录里创建一个以"接口全限定名"的文件,内容为实现类的全限定名，最后一般打成jar包提供给调用方。

2.调用方引入jar包后，可以通过查找这个jar包的META-INF/services/中的配置文件中的接口具体实现类名来进行实例化对象，进行服务调用。

3.调用方使用java.util.ServiceLoder动态装载实现模块，它通过扫描META-INF/services目录下的配置文件找到实现类的全限定名，把类加载到JVM。

4.SPI的实现类必须携带一个不带参数的构造方法；

## 3.SPI优缺点

优点：
1.解耦。减少硬编码import 导入实现类。


缺点：
1.默认JDK的SPI机制的ServiceLoad,是遍历使用SPI获取到的具体实现，实例化各个实现类。在遍历的时候，首先调用driversIterator.hasNext()方法，
这里会搜索classpath下以及jar包中所有的META-INF/services目录下的java.sql.Driver文件，并找到文件中的实现类的名字，注意此时并没有实例化具体的实现类。

不能按需加载，需要遍历所有的实现，并实例化，然后在循环中才能找到我们需要的实现。如果不想用某些实现类，或者某些类实例化很耗时，它也被载入并实例化了，这就造成了浪费。

```text
public Void run() {

        ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);
        Iterator<Driver> driversIterator = loadedDrivers.iterator();

        /* Load these drivers, so that they can be instantiated.
         * It may be the case that the driver class may not be there
         * i.e. there may be a packaged driver with the service class
         * as implementation of java.sql.Driver but the actual class
         * may be missing. In that case a java.util.ServiceConfigurationError
         * will be thrown at runtime by the VM trying to locate
         * and load the service.
         *
         * Adding a try catch block to catch those runtime errors
         * if driver not available in classpath but it's
         * packaged as service and that service is there in classpath.
         */
        try{
            while(driversIterator.hasNext()) {
                driversIterator.next();
            }
        } catch(Throwable t) {
        // Do nothing
        }
        return null;
    }
```

2.并且获取某个实现类的方式不够灵活，只能通过Iterator形式获取，不能根据某个参数来获取对应的实现类。
遍历iterator，遍历到最后，使用最后一个实现配置作为最终的实例。最后一个取决于我们运行时的 ClassPath 配置，在前面加载的jar自然在前，最后的jar里的自然当然也在后面。
当然我们java启动时指定 -cp 如 java -cp a.jar:b.jar:main.jar example.Main 。


3.多个并发多线程使用ServiceLoader类的实例是不安全的。

## 变种SPI 
因为原生SPI机制有全部加载、无法指定加载、并发问题等不足，于是出现了很多变种SPI机制。

### Dubbo SPI
Dubbo 中实现了一套新的 SPI 机制，功能更强大，也更复杂一些。相关逻辑被封装在了 ExtensionLoader 类中，
通过 ExtensionLoader，我们可以加载指定的实现类。Dubbo SPI 所需的配置文件需放置在 META-INF/dubbo 路径下，配置内容如下（以下demo来自dubbo官方文档）。
```text
optimusPrime = org.apache.spi.OptimusPrime
bumblebee = org.apache.spi.Bumblebee
```
与 Java SPI 实现类全限定名配置不同，Dubbo SPI 是通过键值对的方式进行配置，这样我们可以按需加载指定的实现类。另外在使用时还需要在接口上标注 @SPI 注解。
```text
@SPI
public interface Robot {
    void sayHello();
}

public class OptimusPrime implements Robot {
    
    @Override
    public void sayHello() {
        System.out.println("Hello, I am Optimus Prime.");
    }
}

public class Bumblebee implements Robot {

    @Override
    public void sayHello() {
        System.out.println("Hello, I am Bumblebee.");
    }
}


public class DubboSPITest {

    @Test
    public void sayHello() throws Exception {
        ExtensionLoader<Robot> extensionLoader = 
            ExtensionLoader.getExtensionLoader(Robot.class);
        Robot optimusPrime = extensionLoader.getExtension("optimusPrime");
        optimusPrime.sayHello();
        Robot bumblebee = extensionLoader.getExtension("bumblebee");
        bumblebee.sayHello();
    }
}
```
Dubbo SPI 和 JDK SPI 最大的区别就在于支持“别名”，可以通过某个扩展点的别名来获取固定的扩展点。
就像上面的例子中，我可以获取 Robot 多个 SPI 实现中别名为“optimusPrime”的实现，也可以获取别名为“bumblebee”的实现。

### Spring SPI
Spring 的 SPI 配置文件是一个固定的文件 - META-INF/spring.factories，功能上和 JDK 的类似，每个接口可以有多个扩展实现，使用起来非常简单：
并不是每一个springbootstarter都用的这个机制，目前看只有spring-boot-autoconfigure使用该方式。有的用的是JDK的SPI，spring-boot-starter还没用SPI机制

下面是一段 Spring-Boot-AutoConfigure 中 spring.factories 的配置
```text
# Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener

# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.autoconfigure.BackgroundPreinitializer

# Environment Post Processors
org.springframework.boot.env.EnvironmentPostProcessor=\
org.springframework.boot.autoconfigure.integration.IntegrationPropertiesEnvironmentPostProcessor

# Auto Configuration Import Listeners
org.springframework.boot.autoconfigure.AutoConfigurationImportListener=\
org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener

# Auto Configuration Import Filters
org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
org.springframework.boot.autoconfigure.condition.OnBeanCondition,\
org.springframework.boot.autoconfigure.condition.OnClassCondition,\
org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition
```
Spring SPI 中，将所有的配置放到一个固定的文件 spring.factories 中，省去了配置一大堆文件的麻烦。spring.factories文件中每个接口可以有多个实现类。

Spring 也是支持 ClassPath 中存在多个 spring.factories 文件的，加载时会按照 classpath 的顺序依次加载这些 spring.factories 文件，添加到一个 ArrayList 中。由于没有别名，所以也没有去重的概念，有多少就添加多少。

但由于SpringBoot的ClassLoader会优先加载项目中的文件，而非依赖包的文件，于是如果在你的项目中定义个spring.factories文件，那么你项目中的文件会被第一个加载，得到的Factories中，项目中spring.factories里配置的那个实现类也会排在第一个。

比如如果需要拓展某个接口，只需要在你的项目（spring boot）里新建一个META-INF/spring.factories文件，只添加你要的那个配置即可。对于JDK的SPI其实就是把配置放到一起，并且保证了自己写的一定最先加载。

```text
org.springframework.boot.logging.LoggingSystemFactory=\
com.example.log4j2demo.Log4J2LoggingSystem.Factory
```




参考文档：  
https://juejin.cn/post/6844903605695152142
https://jishuin.proginn.com/p/763bfbd2a0f0  
https://cloud.tencent.com/developer/article/1606416  
https://crossoverjie.top/2020/02/24/wheel/cicada8-spi/  
https://www.cnblogs.com/oskyhg/p/10800051.html  
http://qiankunli.github.io/2017/05/02/write_java_framework.html
https://zhuanlan.zhihu.com/p/433228996