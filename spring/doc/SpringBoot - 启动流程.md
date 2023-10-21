
SpringBoot本质是基于Spring框架的一个快速开发框架，是基于“约定优于配置”理念下的最佳实践。

## 适用注解
@Configuration。Spring3.0以后提供的功能，可以适用Java代码替换原来的XML配置文件配置方式。

@SpringBootApplication。这是一个组合注解，由三个注解组成 @SpringBootConfiguration @EnableAutoConfiguration @CompentScan

@SpringBootConfiguration。来源于@Configuration，二者的功能都是将当前类设为配置类，并将类中以@Bean注解标记的方法的实例注入Spring容器。

@EnableAutoConfiguration。整个SpringBoot启动的主角，也是组合注解，关键在于 @Import({AutoConfigurationImportSelector.class}) 。 借助AutoConfigurationImportSelector类
AutoConfigurationImportSelector可以实现读取selector配置的类，其中 List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
可以使用 SpringFactoriesLoader.loadFactoryNames 方法从指定claaspath下读取 META—INF/spring.factories 文件的配置，并返回一个字符串数组，通过这个方法所有自动配置类都会加载到Spring容器中。
因此我们只要按照Spring的写法，把自定义的starter中要加载的类写到 META—INF/spring.factories 中即可。 Selector也能提供对类的按前缀加载、排除。

@CompentScan。本身Spring框架的加载Bean的主要组件，并不是SpringBoot的新功能。定义扫描路径，默认扫描类所在包下的所有符合条件的组件和Bean定义，最终这些Bean会加载到Spring容器中。


## SpringBoot启动流程
建议对main方法打断点，使用debug单步调试。

1.初始化SpringBootApplication
2.运行SpringBootApplication
    2.1.SpringBootApplicationRunListeners 应用启动监控模块
     创建SpringBootApplicationRunListeners，只有一个EventPulishRunListener广播事件监听器，将Spring的Starting方法封装成SpringBootApplicationEvent事件广播出去、
            被SpringApplication中配置的Listener所监听，当这一步骤完成，才会完成真正的启动。比如如果Spring的Bean的加载中有冲突，会有'application event raised applicationfailedevent'的报错。
     --
    2.2.ConfigurationEnvironment 配置环境模块和监听
        2.2.1.创建配置环境        创建StandardEnvironment
        2.2.2.加载属性配置文件    配置configurePropertySource
        2.2.3.配置监听           调用ApplicationListener发送onApplicationEvent事件，通知SpringBoot应用的环境已准备完成。
         ~~
    2.3.ConfigurationApplicationContext 配置应用上下文
        2.3.1.配置应用上下文对象   创建configurationApplicationContext
        2.3.2.配置基本属性         prepareContext方法与Spring上下文关联
        2.3.3.刷新应用上下文       调用refresh(context)方法将通过工厂模式产生应用上下文所需要的Bean
        
## 自动装配方法
### 基于条件的自动装配
SpringFactoriesLoader是其中加载的关键，借助SpringFactoriesLoader “私有协议特性” 将标注@Configuration的JavaConfig全部加载到Spring容器中

@ConditionOnBean : Spring容器中存在指定Bean，则实例化当前Bean。
@ConditionOnClass : Spring容器中存在指定Class，则实例化当前Bean。
@ConditionOnMissingBean : Spring容器中不存在指定Bean，则实例化当前Bean。
@ConditionOnMissingClass : Spring容器中不存在指定Class，则实例化当前Bean。
@ConditionOnResource : 类指定路径是否有指定值，则实例化当前Bean。
···

### 调整自动装配顺序
@AutoConfiuratinAfter  将一个配置类在另一个配置类之后加载。
@AutoConfiuratinBefore  将一个配置类在另一个配置类之前加载。


## SpringBoot常用Starter

* spring-boot-starter-web
可以直接启动一个嵌入式Tomcat服务请求的Web应用服务

* spring-boot-starter-jdbc
会自动触发创建DataSource

* spring-boot-starter-aop
定义不同切面工程

* spring-boot-starter-logging

* spring-boot-starter-security






