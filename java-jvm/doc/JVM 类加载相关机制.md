## 1.classload机制
首先不同jvm实现不同，因此这里只介绍最广泛的hotspot虚拟机。


## 2.JDK默认classload
默认三种classload：
* BootstropClassload 启动类加载器，C++语言编写，jvm启动后初始化，加载%JAVA_HOME%/jre/lib,Xbootclasspath中类
* ExtCLassLoad  拓展类加载器，java语言编写，主要加载 %JAVA_HOME%/jre/lib/ext  
* AppClassLoad  应用程序类加载器，java语言编写，主要加载ClassPath下的类

## 3.双亲委托机制

1 . 当前ClassLoader首先从自己已经加载的类中查询是否此类已经加载，如果已经加载则直接返回原来已经加载的类。

每个类加载器都有自己的加载缓存，当一个类被加载了以后就会放入缓存，等下次加载的时候就可以直接返回了。

2 . 当前classLoader的缓存中没有找到被加载的类的时候，委托父类加载器去加载，父类加载器采用同样的策略，首先查看自己的缓存，然后委托父类的父类去加载，一直到bootstrp ClassLoader.

3 .当所有的父类加载器都没有加载的时候，再由当前的类加载器加载，并将其放入它自己的缓存中，以便下次有加载请求的时候直接返回。

不同类加载器加载的同名类的全限定名不同，因此可以加载同名但是不同内容的类。

## 4.自定义classLoad
定义自已的类加载器分为两步：
1、继承java.lang.ClassLoader
2、重写父类的findClass方法

并且由于Class loadClass(String name, boolean resolve)这个方法不是final，所以是可以override，也即双亲委托是可以打破的。 

## 5.不遵循双亲委托的场景

SPI机制，jdbc的使用，Tomcat使用其实就是bootstrop类要去加载第三方的类。于是引入了线程上下文委托机制。

Tomcat可以运行不同应用程序，当在一个Tomcat中不同应用需要使用同一个包的不同版本时，如果还使用双亲委派机制就会出问题。
于是Tomcat使用了自己的类加载机制，有共享包的Common ClassLoad，有每个应用私有的WebApp ClassLoad，各个项目就是使用各自的Web ClassLoad加载进Tomcat容器的。

Tomcat的加载可以解决不同应用使用同一个包的不同版本，而当一个应用需要使用同一个包的不同版本时，比如一个程序即需要mysql 8的客户端，又需要mysql 5的客户端。
那么解决这个问题就需要应用自己定义类加载器了。

## 常见面试题
Q:双亲委派机制的作用？
  
Q:Tomcat的classloader结构

Q:如何自己实现一个classloader打破双亲委派

Q:Tomcat热部署，热加载,怎么做到的？


参考文档：  
https://www.jianshu.com/p/aa6d1c32d104