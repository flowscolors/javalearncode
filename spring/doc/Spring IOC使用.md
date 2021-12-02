## Spring IOC
Ioc是一种机制，或者说是一种思想（Inversion of Control）。  
 
控制反转一般分为两种类型，依赖注入（Dependency Injection）和依赖查找(Dependency Lookup)，依赖注入应用比较广泛。  
在Spring中的依赖注入一般有三种实现:1.构造器注入 2.setter方法注入 3.接口注入

但是在Spring中，Ioc是一种容器。Spring通过Ioc容器来管理对象的实例化和初始化，以及对象从创建到销毁的整个生命周期。  

Spring中对象都由Ioc容器管理，不需要我们手动new运算符创建对象，由Ioc容器管理的对象称为Spring Bean。Spring Bean就是Java对象，与new运算符创建的对象没有区别。只是Spring通过读取xml文件或者Java注解中的信息来获取哪些对象需要实例化。  

Spring提供两种类型的Ioc容器，即BeanFactory和ApplicationContext容器。

## 1.BeanFactory容器
BeanFactory 是最简单的容器，由 org.springframework.beans.factory.BeanFactory 接口定义，采用懒加载（lazy-load），所以容器启动比较快。BeanFactory 提供了容器最基本的功能。

为了能够兼容 Spring 集成的第三方框架（如 BeanFactoryAware、InitializingBean、DisposableBean），所以目前仍然保留了该接口。

简单来说，BeanFactory 就是一个管理 Bean 的工厂，它主要负责初始化各种 Bean，并调用它们的生命周期方法。

BeanFactory 接口有多个实现类，最常见的是 org.springframework.beans.factory.xml.XmlBeanFactory。使用 BeanFactory 需要创建 XmlBeanFactory 类的实例，通过 XmlBeanFactory 类的构造函数来传递 Resource 对象。如下所示。
```
Resource resource = new ClassPathResource("applicationContext.xml"); 
BeanFactory factory = new XmlBeanFactory(resource); 
```  

## 2.ApplicationContext 容器

ApplicationContext 继承了 BeanFactory 接口，由 org.springframework.context.ApplicationContext 接口定义，对象在启动容器时加载。ApplicationContext 在 BeanFactory 的基础上增加了很多企业级功能，例如 AOP、国际化、事件支持等。

ApplicationContext 接口有两个常用的实现类，具体如下。
1）ClassPathXmlApplicationContext
该类从类路径 ClassPath 中寻找指定的 XML 配置文件，并完成 ApplicationContext 的实例化工作.

2）FileSystemXmlApplicationContext
该类从指定的文件系统路径中寻找指定的 XML 配置文件，并完成 ApplicationContext 的实例化工作。所以可以获取类路径之外的资源，

需要注意的是，BeanFactory 和 ApplicationContext 都是通过 XML 配置文件加载 Bean 的。

二者的主要区别在于，如果 Bean 的某一个属性没有注入，使用 BeanFacotry 加载后，第一次调用 getBean() 方法时会抛出异常，而 ApplicationContext 则会在初始化时自检，这样有利于检查所依赖的属性是否注入。

因此，在实际开发中，通常都选择使用 ApplicationContext，只有在系统资源较少时，才考虑使用 BeanFactory。

参考文档: 
http://c.biancheng.net/spring/ioc.html


## 3. Spring对象注入