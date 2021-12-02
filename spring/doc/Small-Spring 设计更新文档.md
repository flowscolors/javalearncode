
Small-Spring  
* Bean容器 作用域
* 定义&注册 Cglib代理
* 实例化策略 初始化&销毁
* 属性填充  循环依赖
* 资源加载  包扫描
* 上下文 事件监听

## Part 1 从存对象到存BeanDefine的BeanFactory

### 1.创建简单的Bean容器
* Bean定义 BeanDefinition    用于存放 Bean 对象，目前只有Object对象，后续要填充相关属性   
* Bean工厂  BeanFactory      包括了 Bean 的注册、存放、获取  直接用hashmap存每个对象  

### 2.实现Bean的定义、注册、获取
要把Bean的创建交给工厂，而不是new对象一个传给工厂，并且需要考虑单例，即第二次获取可以直接获取。第二次获取是上一次获取的，这个问题在我存对象的时候的毋庸置疑的，因为就是一个对象，但是在我存类的时候，如何还是两次取一个对象就需要单例模式了。
* Bean工厂  BeanFactory     
    1) 不再存对象，而是存类，让BeanFactory去初始化对象 
    2) BeanFactory由一个对象改为一个接口 如何get put，由实现接口的子类完成
    3) 获取Bean的方法改为抽象类AbstractBeanFactory 如果你只想在这里实现某些方法，那就再用一个抽象类，only在这里实现某些方法，让再下一个子类实现他的方法。
* AbstractBeanFactory 
    1) 继承了实现了 SingletonBeanRegistry 的DefaultSingletonBeanRegistry 类，以此来具备单例功能
    2) 又定义了两个抽象方法：getBeanDefinition(String beanName)、createBean(String beanName, BeanDefinition beanDefinition) ，而这两个抽象方法分别由 DefaultListableBeanFactory、AbstractAutowireCapableBeanFactory 实现。
 * AbstractAutowireCapableBeanFactory 目前只是实现createBean放到Bean工厂的功能
 * DefaultListableBeanFactory  默认的Bean工厂，自己实现注册Bean、获取Bean功能 registerBeanDefinition、getBeanDefinition，创建Bean功能使用继承的父类的createBean。
 
 先写底层的BeansException BeanDefinition BeanFactory;  
 再一层一层往下写SingletonBeanRegistry（这里会用一个hashmap存所有单例对象） DefaultSingletonBeanRegistry；
 写AbstractBeanFactory、AbstractAutowireCapableBeanFactory、  BeanDefinitionRegistry、DefaultListableBeanFactory（这里会用一个hashmap存所有的BeanDefine对象，BeanDefine可以是单例，也可以是其他）。
 
 本来最上层的BeanFactory只有getBean一个方法，但实际使用时在各种场景使用不同方法，于是我们实例化各种抽象类最底层的实现类，就能实现各种功能了。

 通过类抽象和类继承完成所有的实现都以职责划分、共性分离以及调用关系定义为标准搭建的类关系。如何根据业务划分不同的接口、抽象类、继承、实现，才是关键能力。
通过这种方法，业务只要初始化的不同的子类，相当于就去使用不同的方法，而不是每个业务去写一个if else去选择一个逻辑去调用。

### 3.基于Cglib实现含构造函数的类实例化策略
目前实现了Bean工厂，把实例化对象交给Bean工厂统一管理。但是我们直接使用的是class.newInstance()，这种情况如果有构造函数就会抛异常了。
```
Caused by: java.lang.InstantiationException: springframework.test.bean.UserService
	at java.lang.Class.newInstance(Class.java:427)
	at springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:16)
	... 67 more
Caused by: java.lang.NoSuchMethodException: springframework.test.bean.UserService.<init>()
	at java.lang.Class.getConstructor0(Class.java:3082)
	at java.lang.Class.newInstance(Class.java:412)
	... 68 more
```
该处设计主要有2部分：
1) 从哪合理的把构造函数的入参信息传递到实例化操作里?  简单的说就是在传参是不止传一个String的name，还要传Object... args 这样一个入参信息  
   在BeanFactory中添加一个getBean(String name,Object... args)方法，用来传参
2) 怎么去实例化含有构造函数的对象?  可以基于 Java 本身自带的方法 DeclaredConstructor  或者使用Cglib的enhancer，基于字节码ASM实现，或者直接使用ASM。  
   添加一个InstantiationStrategy 实例化策略接口，并且需要补充getBean 入参信息，让外部调用时可以传递构造函数的入参并顺利实例化。

新增InstantiationStrategy、SimpleInstantiationStrategy、CglibSubclassingInstantiationStrategy 一个接口、两个实现类实现对对象的实例化逻辑
修改AbstractAutowireCapableBeanFactory 中createBean的逻辑，传入Object[] args参数实例化带构造函数的类。
同样修改AbstractBeanFactory getBean的实现，新增getBean(String name, Object... args)，但是由于目前我们底层使用hashmap，所以用同一key，并不会查出两个对象，因为覆盖了。  
修改UserSerivce，需要保证类有构造方法


### 4.注入属性与依赖对象
我们现在已经创建了Bean工厂、定义和注册Bean、实例化Bean、并按照不同的构造函数去执行不同的实例化策略。  
但是我们真正使用的对象除了构造还有各种属性、并且对于属性的填充除了 int、Long、String，还包括还没有实例化的对象属性，这些都要在放入Bean工厂时解决。

1) 首先这段逻辑的位置肯定是在creatBean里面，并且是上面的JDK、Cglib的newInstance之后，再去补全。
2) 而要在这里补全，前面肯定要把对应的填充属性传进来，所以BeanDefinition 里面要有对应信息。
3) 另外是填充属性信息还包括了 Bean 的对象类型(Student有个同桌的属性也是Student)，也就是需要再定义一个 BeanReference，里面其实就是一个简单的 Bean 名称，在具体的实例化操作时进行递归创建和填充，与 Spring 源码实现一样。Spring 源码中 BeanReference 是一个接口。

因此需要新增加3个类，BeanReference(类引用)、PropertyValue(属性值，String Object描述一个属性)、PropertyValues(属性集合，一个ArrayList，add、get)，分别用于类和其他类型属性填充操作。
主要改动的类是AbstractAutowireCapableBeanFactory，在 createBean 中补全属性填充部分。BeanDefinition,在其中定义PropertyValues，以及get set方法,也即我们把所需要的属性放到BeanDefinition里。

这里需要注意:
> 我们并没有去处理循环依赖的问题，这部分内容较大，后续补充。
> BeanUtil.setFieldValue(bean, name, value) 是 hutool-all 工具类中的方法，你也可以自己实现

## Part 2 从xml注册对象到对象生命周期的拓展流程

### 5.资源加载器解析文件注册对象
把对BeanDefine的定义放到配置文件中读取，而这个资源加载，我希望不只是xml，还能支持远程读取文件等等。
首先描述的地方要有Bean对象的描述、属性信息。剩下的就是Java部分，把Bean描述信息解析后进行注册，把Bean对象注册到Spring容器里。

新增core.io、factory.xml、以及新增多个特殊BeanFactory的接口
* 新增core.io 主要实现资源加载器、xml资源处理类，因为实际不论ClassPath、File、Http文件，最后都是把class文件返回成xml对象，提交给xml资源处理类统一处理。  
** Resource接口 只定义了一个getInputStream的方法  对应ClassPathResource、FileSystemResource、UrlResource三个实现类
** ResourceLoader接口 只定义了一个getResource方法，用来获取上面定义的Resource  对应DefaultResourceLoader一个实现类

* 新增xml解析部分功能
** 新增BeanDefinitionReader接口，用来从ResourceLoader中读取Resource，并进行Bean注册
** 新增AbstractBeanDefinitionReader抽象类，实现BeanDefinitionReader接口getResourceLoader、注册BeanDefine方法
** 新增xml.XmlBeanDefinitionReader 类，继承AbstractBeanDefinitionReader抽象类。 实现用来从各个Resource的InputStream解析成xml格式，读取xml标签，获得对应属性、对象值，构建BeanDefinition。并调用上一part部分的代码生成把BeanDefinition注册到BeanFactory中。

* 新增ConfigurableListableBeanFactory、HierarchicalBeanFactory、ListableBeanFactory等接口，用来为以后组合起来定义不同的BeanFactory

* util工具类  本次只新增了一个ClassUtils，获得当前线程对应的上下文classloader


### 6.实现应用上下文，自动识别、资源加载、扩展机制
目标：
1.对容器中 Bean 的实例化过程添加扩展机制。  
2.Spring 是如何对 xml 加载以及注册Bean对象的操作过程，目前还是需要写代码调用DefaultListableBeanFactory，需要整合。  

很明显，有两个地方可以做修改，
一处在BeanDefine被注册后，但是尚未实例化，这时候直接改BeanDefine即可，对应Spring中的BeanFactoryPostProcess。
另一处是Bean实例化后修改Bean对象，让后面读取的都是新的Bean对象，这部分就与AOP有关了，对应Spring中的BeanPostProcessor。  
同时如果只是添加这两个接口，不做任何包装，那么对于使用者来说还是非常麻烦的。我们希望于开发 Spring 的上下文操作类，把相应的 XML 加载 、注册、实例化以及新增的修改和扩展都融合进去，让 Spring 可以自动扫描到我们的新增服务，便于用户使用。也即我上面说的功能，直接改xml就可以实现，不用写代码。  

新增context部分，bean.factory部分新增对应接口。注意新增的接口在前面部分使用的时候需要修改继承关系。  
** AbstractApplicationContext 应用上下文抽象类  很核心的一个类
** AbstractRefreshableApplicationContext 继承AbstractApplicationContext，提供刷新能力
** AbstractXmlApplicationContext  继承AbstractRefreshableApplicationContext，提供自xml的刷新能力
** ClassPathXmlApplicationContext 继承AbstractXmlApplicationContext，提供来自classPath的xml资源刷新能力
  
* AbstractAutowireCapableBeanFactory中填充字段之后加一个 initializeBean来执行 Bean 的初始化方法和 BeanPostProcessor 的前置和后置处理方法

ApplicationContext 接口的定义是继承 BeanFactory 外新增加功能的接口，它可以满足于自动识别、资源加载、容器事件、监听器等功能，同时例如一些国际化支持、单例Bean自动初始化等，也是可以在这个类里实现和扩充的。

### 7.Bean对象的初始化和销毁方法
当我们的类创建的 Bean 对象，交给 Spring 容器管理以后，这个类对象就可以被赋予更多的使用能力。就像我们在上一章节已经给类对象添加了修改注册Bean定义未实例化前的属性信息修改和实例化过程中的前置和后置处理，这些额外能力的实现，都可以让我们对现有工程中的类对象做相应的扩展处理。

那么除此之外我们还希望可以在 Bean 初始化过程，执行一些操作。比如帮我们做一些数据的加载执行，链接注册中心暴漏RPC接口以及在Web程序关闭时执行链接断开，内存销毁等操作。如果说没有Spring我们也可以通过构造函数、静态方法以及手动调用的方式实现，但这样的处理方式终究不如把诸如此类的操作都交给 Spring 容器来管理更加合适。 

并且和上面一样，我们希望这些初始化、销毁函数也是配置到xml里面的，可以直接使用。  

也即我们在createBean之前调用init-method，在收到destroy-method的调用时，把bean销毁。这两个字段存到到BeanDefine中。  
销毁操作是关于向虚拟机注册钩子，保证在虚拟机关闭之前，执行销毁操作。Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("close！")));    
(●'◡'●)

* 定义初始化和销毁方法的接口
springframework.beans.factory.InitializingBean
springframework.beans.factory.DisposableBean

*  Bean属性定义新增初始化和销毁 BeanDefinition
*  初始化在AbstractAutowireCapableBeanFactory中填充新的方法 在createbean之前执行
   在方法 invokeInitMethods 中，主要分为两块来执行实现了 InitializingBean 接口的操作，处理 afterPropertiesSet 方法。另外一个是判断配置信息 init-method 是否存在，执行反射调用 initMethod.invoke(bean)。这两种方式都可以在 Bean 对象初始化过程中进行处理加载 Bean 对象中的初始化操作，让使用者可以额外新增加自己想要的动作。
*  销毁的回调方法也在AbstractAutowireCapableBeanFactory中定义 createbean时生成
*  DisposableBeanAdapter 适配器的类呢，因为销毁方法有两种甚至多种方式，目前有实现接口 DisposableBean、配置信息 destroy-method，两种方式。而这两种方式的销毁动作是由 AbstractApplicationContext 在注册虚拟机钩子后看，虚拟机关闭前执行的操作动作。
   那么在销毁执行时不太希望还得关注都销毁那些类型的方法，它的使用上更希望是有一个统一的接口进行销毁，所以这里就新增了适配类，做统一处理。
   注意这里的销毁是指JVM都没了，去执行相关操作，也即默认生命周期是一直存在的，不会被GC、不会被干掉。除非回调函数被触发。   

主要完成了关于初始和销毁在使用接口定义 implements InitializingBean, DisposableBean 和在spring.xml中配置 init-method="initDataMethod" destroy-method="destroyDataMethod" 的两种具体在 AbstractAutowireCapableBeanFactory 完成初始方法和 AbstractApplicationContext 处理销毁动作的具体实现过程。 

### 8.定义标记类型Aware接口，实现感知容器对象
我们现在已经可以实现BeanFactoryPostProcessor、BeanPostProcessor、InitializingBean、DisposableBean来对Bean的生命周期做操作。  
但是如果想对Spring框架里的部分进行操作，BeanFactory、ApplicationContext、BeanClassLoader等能力做拓展，首先怎么获取这些功能的参数，其次修改后怎么返回给Spring。  

定义接口 Aware，在 Spring 框架中它是一种感知标记性接口，具体的子类定义和实现能感知容器中的相关对象。也就是通过这个桥梁，向具体的实现类中提供容器服务。
继承 Aware 的接口包括：BeanFactoryAware、BeanClassLoaderAware、BeanNameAware和ApplicationContextAware，当然在 Spring 源码中还有一些其他关于注解的，不过目前我们还是用不到。
当然最后实现还是在AbstractAutowireCapableBeanFactory 的 createBean中创建对应调用操作。  

* 定义标记接口 Aware
* 容器感知类
** BeanFactoryAware  实现此接口，既能感知到所属的 BeanFactory
** BeanClassLoaderAware 实现此接口，既能感知到所属的 ClassLoader
** BeanNameAware   实现此接口，既能感知到所属的 BeanName
** ApplicationContextAware 实现此接口，既能感知到所属的 ApplicationContext
** ApplicationContextAwareProcessor 包处理器，由于 ApplicationContext 的获取并不能直接在创建 Bean 时候就可以拿到，所以需要在 refresh 操作时，把 ApplicationContext 写入到一个包装的 BeanPostProcessor 中去，再由 AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsBeforeInitialization 方法调用

* 修改AbstractApplicationContext的refresh()，refresh() 方法就是整个 Spring 容器的操作过程，与上一章节对比，本次新增加了关于 addBeanPostProcessor 的操作。
* AbstractAutowireCapableBeanFactory 中initializeBean调用Aware感知操作。  

至此我们大致实现了Bean生命周期各个地方的接口，
从XML -> BeanDefine -> BeanFactoryPostProcessor -> Bean实例化 -> Aware感知 -> BeanPostProcessor前置处理 -> 执行Bean初始化方法 -> BeanPostProcessor后置处理 -> 使用Bean -> Bean的销毁方法 

### 9.Bean对象作用域以及FactoryBean的实现和使用
除了单例模式，我们还需要一种能够让使用者定义的Bean 对象。这样用户提供接口处理调用和相应逻辑，就能获得一个所需的Object。  
比如MyBatis，Dao层其实是接口，而实际Service需要调用的Object，这里就需要每次获得xml对应的Object。MyBatis 就是实现了一个 MapperFactoryBean 类，在 getObject 方法中提供 SqlSession 对执行 CRUD 方法的操作。  

所以我们可以把Spring管理的Object分为两者，一种就是之前一直在用的单例，另一种就是FactoryBean,这种对象是一个容器，如果使用原型模式，每次getObject都从FactoryBean中拿了。  
整个的实现过程包括了两部分，一个解决单例还是原型对象，另外一个处理 FactoryBean 类型对象创建过程中关于获取具体调用对象的 getObject 操作。
SCOPE_SINGLETON、SCOPE_PROTOTYPE，对象类型的创建获取方式，主要区分在于 AbstractAutowireCapableBeanFactory#createBean 创建完成对象后是否放入到内存中，如果不放入则每次获取都会重新创建。
createBean 执行对象创建、属性填充、依赖加载、前置后置处理、初始化等操作后，就要开始做执行判断整个对象是否是一个 FactoryBean 对象，如果是这样的对象，就需要再继续执行获取 FactoryBean 具体对象中的 getObject 对象了。整个 getBean 过程中都会新增一个单例类型的判断factory.isSingleton()，用于决定是否使用内存存放对象信息。  

主要添加的逻辑有：Bean 的实例化是单例还是原型模式的判断 、 FactoryBean 的实现
* BeanDefinition 新增scope字段
* XmlBeanDefinitionReader 新增加了关于 Bean 对象配置中 scope 的解析，并把这个属性信息填充到 Bean 定义中。beanDefinition.setScope(beanScope)
* AbstractAutowireCapableBeanFactory 创建、修改Bean时判断单例、原型模式。实际单例模式和原型模式的区别就在于是否存放到内存（单例是singletonObjects这个map）中,如果是原型模式那么就不会存放到内存中，每次获取都重新创建对象，另外非 Singleton 类型的 Bean 不需要执行销毁方法,随正常GC。
* FactoryBean 新增3个方法，获取对象、对象类型，以及是否是单例对象，如果是单例对象依然会被放到内存中。
* FactoryBeanRegistrySupport 实现一个FactoryBean
* AbstractBeanFactory 中 doGetBean 方法新增处理FactoryBean 的逻辑。



### 10.容器事件和事件监听器
在 Spring 中有一个 Event 事件功能，它可以提供事件的定义、发布以及监听事件来完成一些自定义的动作。比如你可以定义一个新用户注册的事件，当有用户执行注册完成后，在事件监听中给用户发送一些优惠券和短信提醒，这样的操作就可以把属于基本功能的注册和对应的策略服务分开，降低系统的耦合。以后在扩展注册服务，比如需要添加风控策略、添加实名认证、判断用户属性等都不会影响到依赖注册成功后执行的动作。

事件的设计本身就是一种观察者模式的实现，它所要解决的就是一个对象状态改变给其他对象通知的问题，而且要考虑到易用和低耦合，保证高度的协作。

在功能实现上我们需要定义出事件类、事件监听、事件发布，而这些类的功能需要结合到 Spring 的 AbstractApplicationContext#refresh()，以便于处理事件初始化和注册事件监听器的操作。

在整个功能实现过程中，仍然需要在面向用户的应用上下文 AbstractApplicationContext 中添加相关事件内容，包括：初始化事件发布者、注册事件监听器、发布容器刷新完成事件。
使用观察者模式定义事件类、监听类、发布类，同时还需要完成一个广播器的功能，接收到事件推送时进行分析处理符合监听事件接受者感兴趣的事件，也就是使用 isAssignableFrom 进行判断。
isAssignableFrom 和 instanceof 相似，不过 isAssignableFrom 是用来判断子类和父类的关系的，或者接口的实现类和接口的关系的，默认所有的类的终极父类都是Object。如果A.isAssignableFrom(B)结果是true，证明B可以转换成为A,也就是A可以由B转换而来。

* 新增ApplicationEvent 定义事件功能的抽象类
* ApplicationContextEvent 定义事件的抽象类，所有的事件包括关闭、刷新，以及用户自己实现的事件，都需要继承这个类。
* ApplicationEventMulticaster 事件广播器，定义了添加监听和删除监听的方法以及一个广播事件的方法 multicastEvent 最终推送时间消息也会经过这个接口方法来处理谁该接收事件。  
* ApplicationEventPublisher 事件发布者接口，所有事件从这里发布
* AbstractApplicationContext 修改，新增了 初始化事件发布者、注册事件监听器、发布容器刷新完成事件，三个方法用于处理事件操作。

## AOP部分
AOP 意为：面向切面编程，通过预编译的方式和运行期间动态代理实现程序功能功能的统一维护。其实 AOP 也是 OOP 的延续，在 Spring 框架中是一个非常重要的内容，使用 AOP 可以对业务逻辑的各个部分进行隔离，从而使各模块间的业务逻辑耦合度降低，提高代码的可复用性，同时也能提高开发效率。  

### 11.基于JDK、CGlib实现AOP切面
关于 AOP 的核心技术实现主要是动态代理的使用，就像你可以给一个接口的实现类，使用代理的方式替换掉这个实现类，使用代理类来处理你需要的逻辑。  
在把 AOP 整个切面设计融合到 Spring 前，我们需要解决两个问题，包括：如何给符合规则的方法做代理，以及做完代理方法的案例后，把类的职责拆分出来。而这两个功能点的实现，都是以切面的思想进行设计和开发。  

其实单独使用某种工具就可以实现AOP的功能，只是为了如果需要同时支持两者或多种AOP底层框架、有效管理所有AOP切面、简化AOP使用、增强切面拓展性··· 
则需要引入AOP框架，Spring就是这么做的，将对AOP的编码解耦为更具有扩展性的各个模块实现。。  
类似对ASM封装做出了Jaasisent、Byte Bubby、BiteKit等。

关键组件：  
* Pointcut  切入点接口，定义用于获取 ClassFilter、MethodMatcher 的两个类，这两个接口获取都是切点表达式提供的内容。
* ClassFilter 定义类匹配类，用于切点找到给定的接口和目标类。
* MethodMatcher 方法匹配，找到表达式范围内匹配下的目标类和方法。
* AspectJExpressionPointcut  切点表达式类，实现matches方法
* AopProxy  AOP实现接口
* JdkDynamicAopProxy JDK动态代理实现AOP，实现上面AOP接口
* Cglib2AopProxy Cglib实现AOP，实现上面AOP接口

通过这样的拆分，可以解耦代理目标对象属性、拦截器属性、方法匹配属性，以及两种不同的代理操作 JDK 和 CGlib 的方式。

### 12.把AOP动态代理，融入到Bean的生命周期
怎么借着 BeanPostProcessor 把动态代理融入到 Bean 的生命周期中，以及如何组装各项切点、拦截、前置的功能和适配对应的代理

可以说Spring-AOP模块是为Bean生命周期管理特化的一个AOP方案。  
为了可以让对象创建过程中，能把xml中配置的代理对象也就是切面的一些类对象实例化，就需要用到 BeanPostProcessor 提供的方法，因为这个类的中的方法可以分别作用与 Bean 对象执行初始化前后修改 Bean 的对象的扩展信息。但这里需要集合于 BeanPostProcessor 实现新的接口和实现类，这样才能定向获取对应的类信息。
但因为创建的是代理对象不是之前流程里的普通对象，所以我们需要前置于其他对象的创建，所以在实际开发的过程中，需要在 AbstractAutowireCapableBeanFactory#createBean 优先完成 Bean 对象的判断，是否需要代理，有则直接返回代理对象。在Spring的源码中会有 createBean 和 doCreateBean 的方法拆分
这里还包括要解决方法拦截器的具体功能，提供一些 BeforeAdvice、AfterAdvice 的实现，让用户可以更简化的使用切面功能。除此之外还包括需要包装切面表达式以及拦截方法的整合，以及提供不同类型的代理方式的代理工厂，来包装我们的切面服务。

Spring 的 AOP 把 Advice 细化了 BeforeAdvice、AfterAdvice、AfterReturningAdvice、ThrowsAdvice，目前我们做的测试案例中只用到了 BeforeAdvice，这部分可以对照 Spring 的源码进行补充测试。

* BeforeAdvice        接口，继承Advice ，定义BeforeAdvice的方法
* MethodBeforeAdvice  接口，继承BeforeAdvice ，在 Spring 框架中，Advice 都是通过方法拦截器 MethodInterceptor 实现的。
* Advisor             接口，定义访问者
* PointcutAdvisor     接口，Advisor 承担了 Pointcut 和 Advice 的组合，Pointcut 用于获取 JoinPoint，而 Advice 决定于 JoinPoint 执行什么操作。
* AspectJExpressionPointcutAdvisor 类，实现PointcutAdvisor接口
* MethodBeforeAdviceInterceptor    方法拦截器，类，实现了 MethodInterceptor 接口
* ProxyFactory        代理工厂，类，主要解决的是关于 JDK 和 Cglib 两种代理的选择问题，有了代理工厂就可以按照不同的创建需求进行控制。  
* DefaultAdvisorAutoProxyCreator 融入Bean生命周期的自动代理创建者，在于 postProcessBeforeInstantiation 方法中，从通过 beanFactory.getBeansOfType 获取 AspectJExpressionPointcutAdvisor 开始。
获取了 advisors 以后就可以遍历相应的 AspectJExpressionPointcutAdvisor 填充对应的属性信息，包括：目标对象、拦截方法、匹配器，之后返回代理对象即可。那么现在调用方获取到的这个 Bean 对象就是一个已经被切面注入的对象了，当调用方法的时候，则会被按需拦截，处理用户需要的信息。
                                                    
通过这套Spring-AOP机制，我们自己不需要手动处理切面、拦截方法等内容，通过注解、xml即可使用。  

## 高级功能部分
### 13.通过注解配置和包自动扫描的方式完成Bean对象的注册                               
其实到本章节我们已经把关于 IOC 和 AOP 全部核心内容都已经实现完成了，只不过在使用上还有点像早期的 Spring 版本，需要一个一个在 spring.xml 中进行配置。这与实际的目前使用的 Spring 框架还是有蛮大的差别，而这种差别其实都是在核心功能逻辑之上建设的在更少的配置下，做到更简化的使用。

这其中就包括：包的扫描注册、注解配置的使用、占位符属性的填充等等，而我们的目标就是在目前的核心逻辑上填充一些自动化的功能。
1.提供一个配置文件，就能把路径下的所有spring.xml都进行加载。  -> 新增的自定义配置属性 component-scan，解析后调用 scanPackage 方法。
2.提供一个注解，使用该注解的类会被加载。  ->  需要自己定义注解进行操作。
3.提供一种占位符机制，对应属性会在被加载时动态生成。  -> BeanFactoryPostProcessor,在所有的 BeanDefinition 加载完成后，实例化 Bean 对象之前，提供修改 BeanDefinition 属性的机制 

### 14.通过注解注入属性对象
在目前 IOC、AOP 两大核心功能模块的支撑下，完全可以管理 Bean 对象的注册和获取，不过这样的使用方式总感觉像是刀耕火种有点难用。因此在上一章节我们解决需要手动配置 Bean 对象到 spring.xml 文件中，改为可以自动扫描带有注解 @Component 的对象完成自动装配和注册到 Spring 容器的操作。
那么在自动扫描包注册 Bean 对象之后，就需要把原来在配置文件中通过 property name="token" 配置属性和Bean的操作，也改为可以自动注入。这就像我们使用 Spring 框架中 @Autowired、@Value 注解一样，完成我们对属性和对象的注入操作。

其实从我们在完成 Bean 对象的基础功能后，后续陆续添加的功能都是围绕着 Bean 的生命周期进行的，比如修改 Bean 的定义 BeanFactoryPostProcessor，处理 Bean 的属性要用到 BeanPostProcessor，完成个性的属性操作则专门继承 BeanPostProcessor 提供新的接口，因为这样才能通过 instanceof 判断出具有标记性的接口。所以关于 Bean 等等的操作，以及监听 Aware、获取 BeanFactory，都需要在 Bean 的生命周期中完成。那么我们在设计属性和 Bean 对象的注入时候，也会用到 BeanPostProcessor 来完成在设置 Bean 属性之前，允许 BeanPostProcessor 修改属性值。  
某种程度上这也是Bean生命周期这个问题为什么经常被问的原因，因为其他功能都是基于它实现的。从监听 Aware、AOP到这次的属性注入也是如此，使用BeanPostProcessor完成。  

像是接口用 instanceof 判断，注解用 Field.getAnnotation(Value.class); 获取，都是相当于在类上做的一些标识性信息，便于可以用一些方法找到这些功能点，以便进行处理。所以在我们日常开发设计的组件中，也可以运用上这些特点。 