

Spring的核心功能就是Bean管理，关于Bean定义了很多可拓展性的接口，通过拓展性接口可以实现很多功能。
比如:动态创建Bean、延迟动态创建Bean、动态注入Bean、动态修改Bean。

### 1.BeanFactory接口
不需要自己实现接口方法，直接调用该接口方法即可。

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
getId 获取spring容器ID
getApplicationName 获取应用明
getDisplayName 获取displayName
getStartupDate 获取启动时间
getAutowireCapableBeanFactory 获取autowire的beanFactory

### 2.FactoryBean接口

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

### 2.BeanPostProcess接口
在 IOC 初始化的时候，会调用的该接口,一个前置一个后置处理方法。
postProcessBeforeInitialization

postProcessAfterInitialization

### BeanFactoryPostProcessor 
Bean创建前对Bean元属性进行修改。
postProcessBeanFactory


### InstantiationAwareBeanPostProcessor 

### Aware系列接口

这些接口都继承自 Aware 接口，并分别定义了自己的接口定义方法。实现这些接口就能得到Spring的Bean 工厂。从而调用getBean方法获取Bean。

BeanNameAware

ApplicationContextAware 

BeanFactoryAware 

### InitialingBean 初始化接口
在属性设置完毕后做一些自定义操作。implements InitialingBean

### DisposableBean 关闭容器接口
在关闭容器前做一些操作。implements DisposableBean，并实现destory()接口，在该接口中实现自己逻辑。

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