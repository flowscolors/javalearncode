
反射机制:所谓的反射机制就是java语言在运行时拥有一项自观的能力。通过这种能力可以彻底的了解自身的情况为下一步的动作做准备。

1.反射可以实现的功能：
在运行时判断任意一个对象所属的类。
在运行时构造任意一个类的对象。
在运行时判断任意一个类所具有的成员变量和方法。
在运行时调用任意一个对象的方法


2.Java的反射机制的实现要借助于4个类：Class，Constructor，Field，Method;

* Class        代表的运行时类对象
* Constructor  类的构造器对象
* Field        类的属性对象
* Method       类的方法对象

```shell script
1. 得到类的方法 4种
Class<?> class = Class.forName("ClassName");  -- 需要实现直到ClassName

Class<?> class = object.getClass()   -- 需要得到对象，从对象得到Class

Class<?> class = Object.class    -- 通过类名加.class直接获得class对象
  
ClassLoader classLoader = this.getClass().getClassLoader();
Class<?> class = classLoader.loadClass(classname);   -- 通过classLoader类加载器得到class

2. 得到构造器的方法

Constructor getConstructor(Class[] params) -- 获得使用特殊的参数类型的公共构造函数， 
 
Constructor[] getConstructors() -- 获得类的所有公共构造函数 
 
Constructor getDeclaredConstructor(Class[] params) -- 获得使用特定参数类型的构造函数(与接入级别无关) 
 
Constructor[] getDeclaredConstructors() -- 获得类的所有构造函数(与接入级别无关)


3.得到字段信息的方法

Field getField(String name) -- 获得命名的公共字段 
 
Field[] getFields() -- 获得类的所有公共字段 
 
Field getDeclaredField(String name) -- 获得类声明的命名的字段 
 
Field[] getDeclaredFields() -- 获得类声明的所有字段

4.得到方法信息的方法

Method getMethod(String name, Class[] params) -- 使用特定的参数类型，获得命名的公共方法 
 
Method[] getMethods() -- 获得类的所有公共方法 
 
Method getDeclaredMethod(String name, Class[] params) -- 使用特写的参数类型，获得类声明的命名的方法 
 
Method[] getDeclaredMethods() -- 获得类声明的所有方法

5.通过Class对象创建实例

TargetObject targetObject = (TargetObject) targetClass.newInstance();

```

3.工作中常用的反射功能

* 反射创建实例。根据类名创建实例（类名可以从配置文件读取，不用new，达到解耦）  

* 反射调用方法。用Method.invoke执行方法