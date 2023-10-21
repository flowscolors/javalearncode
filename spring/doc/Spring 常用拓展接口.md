

Spring的核心功能就是Bean管理，关于Bean定义了很多可拓展性的接口，通过拓展性接口可以实现很多功能。
比如:动态创建Bean、延迟动态创建Bean、动态注入Bean、动态修改Bean。实际Spring自己的AOP Session等功能都是基于这些扩展接口实现的。

## Spring拓展点
### 1.BeanFactory接口
不需要自己实现接口方法，直接调用该接口方法即可。但首先要获得BeanFactory这个对象，可以用下面的Aware方法获得。

常用方法：
getBean 根据beanId/beanClass获取Bean
containsBean bean是否已加载
isSingleton 是否是单例
isTypeMatch 类型是否匹配
getType 获取bean class
getAliases 获取bean别名

### ApplicationContext接口
继承了以下接口：
ApplicationEventPublisher, BeanFactory, EnvironmentCapable, HierarchicalBeanFactory, ListableBeanFactory, MessageSource, ResourceLoader, ResourcePatternResolver
同时也实现了以上接口的所有功能，程序中也不需要自己实现接口方法，直接通过此接口就可以使用以上接口的功能。

常用方法：
applicationContext.getBean("BeanName") 获取Bean对象
applicationContext.getId 获取spring容器ID
applicationContext.getApplicationName 获取应用明
applicationContext.getDisplayName 获取displayName
applicationContext.getStartupDate 获取启动时间
applicationContext.getAutowireCapableBeanFactory 获取autowire的beanFactory

### 2.FactoryBean接口
FactoryBean是spring提供的一个特殊的bean，实现此接口的bean可以往spring容器中添加一个bean。

```java
public interface FactoryBean<T> {
  T getObject() throws Exception;
  Class<?> getObjectType();
  boolean isSingleton();
}
```
该接口定义了3个方法，获取bean实例，获取bean类型，是否是单例。

Spring 在 IOC 初始化的时候，一般的Bean都是直接调用构造方法，而如果该Bean实现了FactoryBean 接口，则会调用该Bean的 getObject 方法获取bean。

这也是Spring 使用此接口构造AOP的原因。在 IOC 调用此方法的时候，返回一个代理，完成AOP代理的创建。

使用方法：
1.定义一个类，实现FactoryBean接口，重写他的 getObject 方法，并注入IOC。
2.在后续从applicationContext中取出这个Bean时，由于实现了FactoryBean接口，就会拿到getObject中new的类的对象。

### 2.BeanPostProcess接口
在 IOC 初始化的时候，会调用的该接口,一个前置一个后置处理方法。用来完成Bean初始化前后需要的一些操作。
postProcessBeforeInitialization

postProcessAfterInitialization

使用方法：
1.定义一个类，实现BeanPostProcess接口，重写postProcessBeforeInitialization或postProcessAfterInitialization方法，返回对象
2.直接使用@Component把类放到IOC里面，应用启动时候加载时候就会触发，可以在Bean初始化之前之后做这件事。

### BeanFactoryPostProcessor 
Bean创建前对Bean元属性进行修改。用来对BeanDefine进行修改，比如把Bean的scope从singleton改变为prototype。
postProcessBeanFactory

使用方法：
1.定义一个类，实现BeanFactoryPostProcessor接口，重写postProcessBeanFactory方法，在方法中从beanFactory中获取Bean，直接修改BeanDefine即可。
2.直接使用@Component把类放到IOC里面，应用启动时候加载时候就会触发，可以在Bean初始化之前之后做这件事。

### InstantiationAwareBeanPostProcessor 
继承BeanPostProcess接口，并增加了三个方法拓展Bean的功能。

postProcessBeforeInstantiation  实例化之前

postProcessAfterInstantiation  实例化之后

postProcessPropertyValues  处理Bean属性之前

使用方法：
1.定义一个类，实现InstantiationAwareBeanPostProcessos接口，重写对应方法，返回对象或执行自己逻辑。
2.直接使用@Component把类放到IOC里面，应用启动时候加载时候就会触发，可以在Bean初始化之前之后做这件事。

### Aware系列接口

这些接口都继承自 Aware 接口，并分别定义了自己的接口定义方法。实现这些接口就能得到Spring的Bean 工厂，从而调用getBean方法获取Bean。

BeanNameAware

ApplicationContextAware 

BeanFactoryAware 

EnvironmentAware 

ResourceLoaderAware  

ApplicationEventPublisherAware  

使用方法：
1.定义一个类，实现Aware接口，重写对应方法，相当于从对应方法的入参中直接获取需要的BeanFactory、ApplicationContext等，执行对应逻辑后。
2.直接使用@Component把类放到IOC里面，应用启动时候加载时候就会触发，可以在Bean初始化之前之后做这件事。

### InitialingBean 初始化接口
在属性设置完毕后做一些自定义操作。implements InitialingBean

使用方法：
1.定义一个类，implements InitialingBean，重写对应方法afterPropertiesSet，在属性注入后执行某些操作。  
2.直接使用@Component把类放到IOC里面，应用启动时候加载时候就会触发，可以在Bean初始化之前之后做这件事。

### DisposableBean 关闭容器接口
允许在容器销毁该bean的时候获得一次回调。implements DisposableBean，并实现destory()接口，在该接口中实现自己逻辑。

### Environment
当前应用正在运行环境的接口，通过此接口可以获得配置文件和属性。该接口还继承了PropertyResolver，可以获取placeholder中的属性值。

常用方法:  
getActiveProfiles 获取当前激活的环境
getDefaultProfiles 获取当前默认环境
acceptsProfiles 检测环境是否处于激活状态
containsProperty 是否包含属性key
getProperty 根据key获取属性
getRequiredProperty 根据key获取属性，若值不存在则抛出IllegalStateException
resolvePlaceholders 处理el表达式，并获取对应值。例如：传入${spring.profiles.active}，就可以获取对应值。跟getProperty不同的是，getProperty需要的是具体key名称，而不是表达式。
resolveRequiredPlaceholders 同resolvePlaceholders，不过值不存在会抛出IllegalStateException

参考文档：  
https://cloud.tencent.com/developer/article/1486121
https://segmentfault.com/a/1190000019786880
https://blog.csdn.net/woheniccc/article/details/80047226
https://www.cnblogs.com/myitnews/p/14017642.html

## SpringBoot拓展点
SpringBoot可以使用各种注解进行拓展了。
@EnableAutoConfiguration 保证自动化配置，配置好类文件地址即可。

SpringApplicationRunListener 在整个启动流程中，作为监听者接受不同执行点的事件通知。直接获取到使用即可。

ApplicationContextInitializer 对ApplicationContext进行处理。

CommandLineRunner 在Spring加载之后，SpringBoot完成初始化之后执行，可以认为是main方法的最后一步。属于SpringBoot应用特定的回调拓展接口。










