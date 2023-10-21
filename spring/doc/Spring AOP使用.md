## 1.AOP
AOP 的全称是“Aspect Oriented Programming”，即面向切面编程，和 OOP（面向对象编程）类似，也是一种编程思想。注意这并不是一个技术实现，而是一个思想。

因为OOP的继承、多态解决了纵向拓展。而横向拓展则无法很好支撑，所以有了AOP来解决横向拓展的能力，比如在所有service中开启事务。可以说AOP是OOP的补充。

AOP 采取横向抽取机制（动态代理），取代了传统纵向继承机制的重复性代码，其应用主要体现在事务处理、日志管理、权限控制、异常处理等方面。主要作用是分离功能性需求和非功能性需求，使开发人员可以集中处理某一个关注点或者横切逻辑，减少对业务代码的侵入，增强代码的可读性和可维护性。

简单的说，AOP 的作用就是保证开发者在不修改源代码的前提下，为系统中的业务组件添加某种通用功能。AOP 就是代理模式的典型应用。  


目前最流行的 AOP 框架有两个，分别为 Spring AOP 和 AspectJ。

* Spring AOP 是基于 AOP 编程模式的一个框架，它能够有效的减少系统间的重复代码，达到松耦合的目的。Spring AOP 使用纯 Java 实现，不需要专门的编译过程和类加载器，在运行期间通过代理方式向目标类植入增强的代码。有两种实现方式：基于接口的 JDK 动态代理和基于继承的 CGLIB 动态代理。
    ** Spring JDK 动态代理需要实现 InvocationHandler 接口，重写 invoke 方法，客户端使用 Java.lang.reflect.Proxy 类产生动态代理类的对象。
    ** CGLIB（Code Generation Library）是一个高性能开源的代码生成包，它被许多 AOP 框架所使用，其底层是通过使用一个小而快的字节码处理框架 ASM（Java 字节码操控框架）转换字节码并生成新的类。
* AspectJ 是一个基于 Java 语言的 AOP 框架，从 Spring 2.0 开始，Spring AOP 引入了对 AspectJ 的支持。AspectJ 扩展了 Java 语言，提供了一个专门的编译器，在编译时提供横向代码的植入。编译期就有代码植入，一般认为这种方法效率更高。在新版本的 Spring 框架中，建议使用 AspectJ 方式开发 AOP。使用方式一般有2种:  
    ** 基于 XML 的声明式 AspectJ
    ** 基于 注解 的声明式 AspectJ 
  

为了更好地理解 AOP，我们需要了解一些它的相关术语。这些专业术语并不是 Spring 特有的，有些也同样适用于其它 AOP 框架，如 AspectJ。它们的含义如下表所示。

名称	说明
Joinpoint（连接点）	指那些被拦截到的点，在 Spring 中，指可以被动态代理拦截目标类的方法。
Pointcut（切入点）	指要对哪些 Joinpoint 进行拦截，即被拦截的连接点。
    execute(public * * (..)) 任意公共方法的执行
    execute(* set* (..))     以set开始的方法的执行
    execute(* com.xyz.service.*.* (..))     定义service包里任意方法的执行
Advice（通知）	指拦截到 Joinpoint 之后要做的事情，即对切入点增强的内容。
Target（目标）	指代理的目标对象。
Weaving（植入）	指把增强代码应用到目标上，生成代理对象的过程。
Proxy（代理）	指生成的代理对象。
Aspect（切面）	切入点和通知的结合。 @Aspect定义一个Java类为切面类。
Advice 直译为通知，也有的资料翻译为“增强处理”，共有 5 种类型，如下表所示。

通知	说明
before（前置通知）	通知方法在目标方法调用之前执行
after（后置通知）	通知方法在目标方法返回或异常后调用
after-returning（返回后通知）	通知方法会在目标方法返回后调用
after-throwing（抛出异常通知）	通知方法会在目标方法抛出异常后调用
around（环绕通知）	通知方法会将目标方法封装起来

参考文档：  
http://c.biancheng.net/spring/aop.html


## Spring AOP
所以实际上Spring AOP内含了JDK动态代理、CGlib动态代理、AspectJ动态代理三种底层支持。


