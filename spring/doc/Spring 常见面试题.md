
Q:Spring中用到的设计模式？
A：简单工厂BeanFactory  
工厂方法FactoryBean  
策略模式访问资源  
观察者模式Event


Q:Spring怎么解决循环依赖？
A:循环依赖一般有三种情况，自己依赖自己，A依赖B、B依赖A，A依赖B、B依赖C、C依赖A。前两者都很容易发现，第三种则因为实际会较为隐蔽而不易发现。Spring解决办法就是三级缓存。
Spring通过三级缓存解决了循环依赖，其中一级缓存为单例池（singletonObjects）,二级缓存为早期曝光对象earlySingletonObjects，三级缓存为早期曝光对象工厂（singletonFactories）。
当A、B两个类发生循环引用时，在A完成实例化后，就使用实例化后的对象去创建一个对象工厂，并添加到三级缓存中，如果A被AOP代理，那么通过这个工厂获取到的就是A代理后的对象，如果A没有被AOP代理，那么这个工厂获取到的就是A实例化的对象。当A进行属性注入时，会去创建B，同时B又依赖了A，所以创建B的同时又会去调用getBean(a)来获取需要的依赖，此时的getBean(a)会从缓存中获取
1.singletonObjects，一级缓存，存储的是所有创建好了的单例Bean
2.earlySingletonObjects，完成实例化，但是还未进行属性注入及初始化的对象
3.singletonFactories，提前暴露的一个单例工厂，二级缓存中存储的就是从这个工厂中获取到的对象

```shell script
@Component
public class A {
    // A中注入了B
    @Autowired
    private B b;
}

@Component
public class B {
    // B中也注入了A
    @Autowired
    private A a;
}
```
A注入B，第一次创建，在哪个缓冲中都没有，调用 getSingleton(beanName, singletonFactory) 创建Bean。通过createBean方法返回的Bean最终被放到了一级缓存，也就是单例池中。
A首先走完实例化，并放到三级缓冲中，开始为A进行属性注入，在注入时发现A依赖了B，那么这个时候Spring又会去getBean(b)，然后反射调用setter方法完成属性注入。而B创建需要A，此时不管A是构造器注入还是setter注入都能在三级缓冲中找到对象，可以正常生成B。  

不过此时注入到B的A，是通过getEarlyBeanReference方法提前暴露出去的一个对象，还不是一个完整的Bean，实际上就是调用了后置处理器的getEarlyBeanReference。所以在一个简单的循环依赖流程中，使用二级缓冲，把创建完未初始化的A给B就可以。

而之所以要三级缓存，是为了解决AOP的问题。此时B中注入的A将是一个代理对象而不是A的实例化阶段创建后的对象。

参考文档：https://developer.aliyun.com/article/766880

Q:常用的注解有哪些？
A:@Service、@Component
@Autowired
@AllArgsConstructor
@Configuration
@Primary

Q:Spring事务传播？
A:事务传播的7种模式，默认是PROPAGATION_REQUIRED。而默认情况下会导致两个都用了事务注解的方法，A中调用B，B的事务不被触发，因为此时是如果存在一个事务，则支持当前事务。如果没有事务则开启
常用的还有NOT_SUPPORTED 总是非事务的执行，适用于站内信、网页提示信息、短信、邮件等，出现异常也要继续执行业务。EQUIRES_NEW  总是创建新的事物执行，比如审计日志，出错了不记录但是要继续执行。

Q:Spring和SpringBoot的区别？
Spring更类似于CNCF社区，Spring提供核心模块，各个公司和个人可以根据Spring核心模块开发自己的功能。
SpringBoot更类似于一个开发脚手架，用来帮助开发人员在开发过程中搭建工程。类似Vue、Maven、Netty、Django。
当然两者的核心特点都由减少复用、不要重新造轮子等。

Q:@Bean注解、@Component注解区别
A:两者都是把PoJo对象注入到Spring中，由Spring进行管理。
1.作用对象不同，@Bean作用于方法，@Component作用于类。
2.@Component 通常是通过路径扫描来自动侦测以及自动装配到 Spring 容器中。@Bean 注解通常是我们在标有该注解的方法中定义产生这个 bean,@Bean告诉Spring这个方法会返回一个对象，该对象要注册成Spring上下文的Bean。
3.@Bean 注解比 @Component 注解的自定义性更强，而且很多地方我们只能通过 @Bean 注解来注册 bean。比如当我们引用第三方库中的类需要装配到 Spring 容器时，只能通过 @Bean 来实现。

Q:DispatchServlet原理
A:Spring MVC组件， 当DispatcherServlet接到请求时，他先回查找适当的处理程序来处理请求。一旦DispatcherServlet选择了适当的控制器，它就会调用这个控制器来处理请求。 

---
Q:Spring IOC和AOP说一下你的理解?
A:IOC和AOP可以说是Spring最核心的两个功能了，后面不计其数的功能、拦截器、事务、缓存都是基于这两个功能实现的。
IOC实现了把所有Bean在一个地方管理，并通过依赖注入提供能力。
AOP实现了对任意Bean实体的方法的覆盖。

Q:spring中bean的生命周期是怎样的？
A:总的大概有11个左右的步骤，大致分两块，Bean的声明周期：实例化、属性注入、初始化、销毁。 各类拓展点：Aware接口（注入BeanName、ClassLoader、BeanFactory）、BeanPostProcessor、InitializingBean、init-method 。
首先是Spring在创建Bean的过程中分为三步：
1.实例化，对应方法：AbstractAutowireCapableBeanFactory中的createBeanInstance方法   new对象
2.属性注入，对应方法：AbstractAutowireCapableBeanFactory的populateBean方法   填充属性
3.初始化，对应方法：AbstractAutowireCapableBeanFactory的initializeBean   执行初始化放啊

Bean生命周期最后的销毁，如果是单例，则和Spring生命周期一致。无法手工销毁，singleton（全局的）是随着spring的存亡而存亡，比如调用applicationContext.close()，
但是可以注册些回调函数，在销毁前调用，DisposableBean 和 destory-method ···。

Q:属性注入和构造器注入哪种会有循环依赖的问题？
A:构造器注入会有循环依赖的问题，而单例的属性注入由于可以走三层缓存拿到先生成的Bean实体，所以运行时不会报错。

Q:Spring beanFactory和factoryBean区别?
A:beanFactory是管理Bean的集合，内含实际的所有bean以及对其的操作方法。

factoryBean是一种Bean模板，不用这个直接写Bean也可以，因为通过XML创建Bean比较复杂，需要提供大量配置信息。于是Spring提供了一个org.springframework.bean.factory.FactoryBean的工厂类接口，用户可以通过实现该接口定制实例化Bean的逻辑。
可以用该Bean生成PoJo对象，直接拿一个对象当作getObject的对象，相当于提供初始化的预计结果，简化操作，Spring默认提供了70多种factoryBean。

Q:Spring的beanpostprocesser和refresh方法
A:分别是最经典的Spring拓展接口，有before、after两种方法，会在bean初始化前后执行。以及Spring中一个模板方法，Spring中没有实现，留下注释交给子类实现，Spring会执行子类实现的方法，

---

Q:Spring AOP的实现？  
A:AOP的实现有2种方式，JDK动态代理和CGLIB字节码代理，一般除了一些全局配置文件来配置，就是在代理类
JDK动态代理：通过java.lang.reflect.Proxy类的newProxyInstance()方法生成反射代理类。
CGLIB代理:采用Enhancer类的create()方法生成代理类，底层利用字节码框架ASM，在内存中生成一个需要被代理类的子类。
而这两者又都是利用IOC中的BeanPostProcessor接口中的两个方法 postPorcessBeforeInitializatin()、postProcessAfterInitialization() ，通过实现此接口的2个方法来在Bean初始化前后做一些工作。
AOP就是在Bean初始化并检测到连接点、切入点Joinpoint、Pointcut时，执行相应Java反射和动态代理把类修改成代理类。

Q:java动态代理和cglib动态代理的区别（经常结合spring一起问所以就放这里了）,优缺点，怎么实现方法的调用的  
A:JDK动态代理：通过java.lang.reflect.Proxy类的newProxyInstance()方法生成反射代理类。
CGLIB代理:采用Enhancer类的create()方法生成代理类，底层利用字节码框架ASM，在内存中生成一个需要被代理类的子类。
而这两者又都是利用IOC中的BeanPostProcessor接口中的两个方法 postPorcessBeforeInitializatin()、postProcessAfterInitialization() ，通过实现此接口的2个方法来在Bean初始化前后做一些工作。
AOP就是在Bean初始化并检测到连接点、切入点Joinpoint、Pointcut时，执行相应Java反射和动态代理把类修改成代理类。

JDK动态代理只能对实现了接口的类生成代理，而不能针对类。
CGLIB是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法，因为是继承，所以该类或方法最好不要声明成final。

Q:@Aspect和普通AOP区别
A:当启动了@AspectJ支持后，在Spring容器中配置一个带@Aspect注释的Bean，Spring将会自动识别该 Bean，并将该Bean作为切面Bean处理。
可以说@Aspect是Sprinig中的注解方式，如果不用这种开箱即用的方式，自己用AspectJ写AOP代码也是可以的，不过麻烦的是，使用AspectJ，需要通过.aj文件来创建切面，并且需要使用ajc（Aspect编译器）来编译代码。。
AspectJ是在编译期间将切面代码编译到目标代码的，属于静态代理；Spring AOP是在运行期间通过代理生成目标类，属于动态代理。

Q:自定义拦截器和Aop那个先执行
A:在Spring中程序是先进过滤器，再进拦截器，最后进切面。Filter (do.chain...)-> Interceptor ...->AOP(被spring管理的bean)...->Interceptor...->Filter
filter只是适用于web中，和框架无关，依赖于Servlet容器，利用Java的回调机制进行实现。
Interfactor是基于Java的反射机制（AOP思想）进行实现，不依赖Servlet容器。
AOP功能更强大，封装更加细致，需要单独引用Jar包。

Q:拦截器和过滤器区别？
A:区别：
* 作用域不同
** 过滤器依赖于servlet容器，只能在 servlet容器，web环境下使用
** 拦截器依赖于spring容器，可以在spring容器中调用，不管此时Spring处于什么环境
* 细粒度的不同
*  过滤器的控制比较粗，只能在请求进来时进行处理，对请求和响应进行包装
** 拦截器提供更精细的控制，可以在controller对请求处理之前或之后被调用，也可以在渲染视图呈现给用户之后调用
* 中断链执行的难易程度不同
** 拦截器可以 preHandle方法内返回 false 进行中断
** 过滤器就比较复杂，需要处理请求和响应对象来引发中断，需要额外的动作，比如将用户重定向到错误页面

拦截器相比过滤器有更细粒度的控制，依赖于Spring容器，可以在请求之前或之后启动，过滤器主要依赖于servlet，过滤器能做的，拦截器基本上都能做。

Q:AOP的使用ASpect的增强方法的顺序
A:注解上加@Order。


---
Q:使用Spring遇到的一些问题？
A:spring启动错误Singleton bean creation not allowed while the singletons of this factory are indestruction。
出现原因：单例的bean在创建的时候，容器已经处于销毁阶段，生命周期不同，不允许再次创建生产Bean。
线程A把一定任务放入线程池，然后返回。由于异步原因，任务提交给线程池后，线程A结束，开始销毁Bean容器。此时线程池中获取Bean的操作就会失败。
此时需要同步，等线程池任务执行完再进行退出。由于一般的@Controller、@Service都是单例，生命周期很长，故不会遇到该情况。除非ApplicationContext.close()。

参考文档：https://blog.csdn.net/chenwiehuang/article/details/101532591
https://stackoverflow.com/questions/15017746/error-creating-bean-with-name-and-singleton-bean-creation-not-allowed
https://www.standbyside.com/2018/12/05/bean-creation-not-allowed-exception/




