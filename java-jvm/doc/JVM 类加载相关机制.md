## 1.classload机制
首先不同jvm实现不同，因此这里只介绍最广泛的hotspot虚拟机。


## 2.JDK默认classload
默认三种classload：
* Bootstrop Classload  C++语言编写，jvm启动后初始化，加载%JAVA_HOME%/jre/lib,Xbootclasspath中类
* ExtCLassLoad  java语言编写，主要加载 %JAVA_HOME%/jre/lib/ext  
3.AppClassLoad  java语言编写，主要加载ClassPath下的类

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

SPI机制，jdbc的使用，其实就是bootstrop类要去加载第三方的类。于是引入了线程上下文委托机制。


参考文档：  
https://www.jianshu.com/p/aa6d1c32d104