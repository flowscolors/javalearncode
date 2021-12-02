## 1.Bean定义
由 Spring IoC 容器管理的对象称为 Bean，Bean 根据 Spring 配置文件中的信息创建。  

Spring 配置文件支持两种格式，即 XML 文件格式和 Properties 文件格式。
Properties 配置文件主要以 key-value 键值对的形式存在，只能赋值，不能进行其他操作，适用于简单的属性配置。
XML 配置文件是树形结构，相对于 Properties 文件来说更加灵活。XML 配置文件结构清晰，但是内容比较繁琐，适用于大型复杂的项目。

通常情况下，Spring 的配置文件使用 XML 格式。XML 配置文件的根元素是 <beans>，该元素包含了多个子元素 <bean>。每一个 <bean> 元素都定义了一个 Bean，并描述了该 Bean 如何被装配到 Spring 容器中。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
    <bean id="helloWorld" class="net.biancheng.HelloWorld">
        <property name="message" value="Hello World!" />
    </bean>
</beans>
```
上述代码中，使用 id 属性定义了 Bean，并使用 class 属性指定了 Bean 对应的类。
<bean>可以包含很多属性，常用如下:  
id    Bean 的唯一标识符，Spring 容器对 Bean 的配置和管理都通过该属性完成。  
name  name 属性中可以为 Bean 指定多个名称，每个名称之间用逗号或分号隔开。Spring 容器可以通过 name 属性配置和管理容器中的 Bean。  
class 该属性指定了 Bean 的具体实现类，它必须是一个完整的类名，即类的全限定名。  
scope 用于设定 Bean 实例的作用域，属性值可以为 singleton（单例）、prototype（原型）、request、session 和 global Session。其默认值是 singleton  
property <bean>元素的子元素，用于调用 Bean 实例中的 setter 方法来属性赋值，从而完成依赖注入。该元素的 name 属性用于指定 Bean 实例中相应的属性名  
init-method 容器加载 Bean 时调用该方法，类似于 Servlet 中的 init() 方法  
destroy-method  容器删除 Bean 时调用该方法，类似于 Servlet 中的 destroy() 方法。该方法只在 scope=singleton 时有效  
lazy-init  懒加载，值为 true，容器在首次请求时才会创建 Bean 实例；值为 false，容器在启动时创建 Bean 实例。该方法只在 scope=singleton 时有效  

## 2.Bean作用域
Spring 容器在初始化一个 Bean 实例时，同时会指定该实例的作用域。Spring 5 支持以下 6 种作用域。
1）singleton
默认值，单例模式，表示在 Spring 容器中只有一个 Bean 实例，Bean 以单例的方式存在。
2）prototype
原型模式，表示每次通过 Spring 容器获取 Bean 时，容器都会创建一个 Bean 实例。
3）request
每次 HTTP 请求，容器都会创建一个 Bean 实例。该作用域只在当前 HTTP Request 内有效。
4）session
同一个 HTTP Session 共享一个 Bean 实例，不同的 Session 使用不同的 Bean 实例。该作用域仅在当前 HTTP Session 内有效。
5）application
同一个 Web 应用共享一个 Bean 实例，该作用域在当前 ServletContext 内有效。
类似于 singleton，不同的是，singleton 表示每个 IoC 容器中仅有一个 Bean 实例，而同一个 Web 应用中可能会有多个 IoC 容器，但一个 Web 应用只会有一个 ServletContext，也可以说 application 才是 Web 应用中货真价实的单例模式。
6）websocket
websocket 的作用域是 WebSocket ，即在整个 WebSocket 中有效。

request、session、application、websocket 和  global Session 作用域只能在 Web 环境下使用，如果使用 ClassPathXmlApplicationContext 加载这些作用域中的任意一个的 Bean，就会抛出IllegalStateException异常。  



## 3.Bean生命周期

普通Java对象，new的时候创建对象，没有任何引用的时候垃圾回收机制回收。  
而Spring Ioc容器托管的对象，生命周期则完全由容器控制。可以说生命周期复杂了一个级别，大致有以下4步:  
Bean的定义 -> Bean的初始化 -> Bean的使用 -> Bean的销毁  
并且Spring会根据Bean的作用域来选择管理模式，对于完全由Spring管理的，singleton 作用域的 Bean，Spring可以完全控制，精确知道Bean是何时被创建，何时初始化完成，何时被销毁的。  
而对于prototype 作用域的Bean，Spring只负责创建，当容器创建了Bean实例后，Bean实例就交给了客户端代码管理，Spring容器不再跟踪其生命周期。

Bean 生命周期的整个执行过程描述如下。
1.Spring 启动，查找并加载需要被 Spring 管理的 Bean，并实例化 Bean。
2.利用依赖注入完成 Bean 中所有属性值的配置注入。
3.如果 Bean 实现了 BeanNameAware 接口，则 Spring 调用 Bean 的 setBeanName() 方法传入当前 Bean 的 id 值。
4.如果 Bean 实现了 BeanFactoryAware 接口，则 Spring 调用 setBeanFactory() 方法传入当前工厂实例的引用。
5.如果 Bean 实现了 ApplicationContextAware 接口，则 Spring 调用 setApplicationContext() 方法传入当前 ApplicationContext 实例的引用。
6.如果 Bean 实现了 BeanPostProcessor 接口，则 Spring 调用该接口的预初始化方法 postProcessBeforeInitialzation() 对 Bean 进行加工操作，此处非常重要，Spring 的 AOP 就是利用它实现的。
7.如果 Bean 实现了 InitializingBean 接口，则 Spring 将调用 afterPropertiesSet() 方法。
8.如果在配置文件中通过 init-method 属性指定了初始化方法，则调用该初始化方法。
9.如果 BeanPostProcessor 和 Bean 关联，则 Spring 将调用该接口的初始化方法 postProcessAfterInitialization()。此时，Bean 已经可以被应用系统使用了。
10.如果在 <bean> 中指定了该 Bean 的作用域为 singleton，则将该 Bean 放入 Spring IoC 的缓存池中，触发 Spring 对该 Bean 的生命周期管理；如果在 <bean> 中指定了该 Bean 的作用域为 prototype，则将该 Bean 交给调用者，调用者管理该 Bean 的生命周期，Spring 不再管理该 Bean。
11.如果 Bean 实现了 DisposableBean 接口，则 Spring 会调用 destory() 方法销毁 Bean；如果在配置文件中通过 destory-method 属性指定了 Bean 的销毁方法，则 Spring 将调用该方法对 Bean 进行销毁。

对于大部分过程已经由Spring自己代码管理的Bean生命周期，我们想要做一些操作就只能基于Spring提供的部分接口，如下:  

* 对于Bean的初始化回调和销毁回调，Spring 官方提供了 3 种方法实现初始化回调和销毁回调：  
**  实现 InitializingBean 和 DisposableBean 接口；  
**  在 XML 中配置 init-method 和 destory-method；  
**  使用 @PostConstruct 和 @PreDestory 注解。  

* 对于Bean的初始化前后，可以执行BeanPostProcessor
** postProcessBeforeInitialization 在 Bean 实例化、依赖注入后，初始化前调用。    
** postProcessAfterInitialization  在 Bean 实例化、依赖注入、初始化都完成后调用。


在一个 Bean 中有多种生命周期回调方法时，优先级为：注解 > 接口 > XML。
>不建议使用接口和注解，这会让 pojo 类和 Spring 框架紧耦合。


## 4.基于注解装配Bean
在 Spring 中，尽管可以使用 XML 配置文件实现 Bean 的装配工作，但如果应用中 Bean 的数量较多，会导致 XML 配置文件过于臃肿，从而给维护和升级带来一定的困难。

Java 从 JDK 5.0 以后，提供了 Annotation（注解）功能，Spring 2.5 版本开始也提供了对 Annotation 技术的全面支持，我们可以使用注解来配置依赖注入。实际开发也很常用这种方式。
注解是Java提供的功能，自定义注解是Java的功能，Spring中提供了很多注解给用户使用。

Spring 中常用的注解如下。
1）@Component
可以使用此注解描述 Spring 中的 Bean，但它是一个泛化的概念，仅仅表示一个组件（Bean），并且可以作用在任何层次。使用时只需将该注解标注在相应类上即可。
2）@Repository
用于将数据访问层（DAO层）的类标识为 Spring 中的 Bean，其功能与 @Component 相同。
3）@Service
通常作用在业务层（Service 层），用于将业务层的类标识为 Spring 中的 Bean，其功能与 @Component 相同。
4）@Controller
通常作用在控制层（如 Struts2 的 Action、SpringMVC 的 Controller），用于将控制层的类标识为 Spring 中的 Bean，其功能与 @Component 相同。
5）@Autowired
可以应用到 Bean 的属性变量、属性的 setter 方法、非 setter 方法及构造函数等，配合对应的注解处理器完成 Bean 的自动配置工作。默认按照 Bean 的类型进行装配。  
autowire实际是一种依赖关系注入的表现，由Spring把一个Bean注入到另一个Bean的Property中，比如常用的把service层注入到controller层中。  
6）@Resource
作用与 Autowired 相同，区别在于 @Autowired 默认按照 Bean 类型装配，而 @Resource 默认按照 Bean 实例名称进行装配。

@Resource 中有两个重要属性：name 和 type。

Spring 将 name 属性解析为 Bean 的实例名称，type 属性解析为 Bean 的实例类型。如果指定 name 属性，则按实例名称进行装配；如果指定 type 属性，则按 Bean 类型进行装配。如果都不指定，则先按 Bean 实例名称装配，如果不能匹配，则再按照 Bean 类型进行装配；如果都无法匹配，则抛出 NoSuchBeanDefinitionException 异常。



## 5.bean加载过程




## 6.bean加载提供接口


DisposableBean  卸载Bean接口，当执行对应的操作时触发清理

ApplicationContextAware接口，需要实现类实现该接口

ApplicationEventPublisherAware接口，需要实现类实现该接口

PS:其他常用接口：
CommandLineRunner接口，实现该接口的run方法，则可在java应用启动的时候执行该段逻辑。

GlobalFilter接口，springcloud Gateway应用内部接口，用来提供WebServer拦截器

## 7.bean加载的实际使用的问题

* 1.Bean替换。Service注解生成的Bean与XML定义的Bean重名，并且XML的Bean替换了注解定义的对象。  
解决办法是要让整个系统中只有一个属性注入成功的queryPartnerImpl对象，途径有如下几种： 
1）删除@Service注解：这样只有一个bean被注入了。  
2）扫描隔离：通过配置xml中属性use-default-filters并配合include-filter/exclude-filter实现扫描过滤，只扫描指定注解。




参考文档：  
https://tech.meituan.com/2016/09/30/mt-trip-springmvc-service-annotation-problem-research.html
