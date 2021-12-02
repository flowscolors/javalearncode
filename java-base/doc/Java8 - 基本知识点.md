
### 浅拷贝和深拷贝的用法
list作为一个数组对象，是最容易进行浅拷贝的操作。注意等于直接赋值的话两个对象是相等的，同一个引用。
浅拷贝的话，list2是用new语句创建出来的新的对象，因此list1和list2两个对象不相等，list.add(), list.remove()这些操作，不会影响到另一个list。
                                                 
而Lambda进行的迭代则不会涉及到原List。

参考文档：  
https://zhuanlan.zhihu.com/p/350219671

### 接口与override
在接口实现类中，加不加override注解都可以，编译器都可以编译通过。也即编译器不会判断你是不是正确重写了父类中的方法，入参、返回、控制类型。  
如果重写的参数与父类不同，编译不会报错，但是运行会报错。认为实现类的是一个新的方法，这样调用接口时就找不到方法，报错。  
但是写override注解，程序会判断是否正确重写了父类方法，并且自动屏蔽父类方法。  
一般使用现代编辑器比如IDEA都会给你自动提示要加@Override注解了。  

### String的不变性
在 Java 中，字符串是一个常量，我们一旦创建了一个 String 对象，就无法改变它的值，它的内容也就不可能发生变化（不考虑反射这种特殊行为）。  比如：
String s = "abc";
s = "ab";
看上去是改变了字符串的值，实际是新建了一个字符串"ab",并把s的引用指向"ab",原来的"abc"对象不变。
查看String源码，值靠private final char value[]; value一旦赋值，无法修改，且除构造函数，没有其他方法修改value里内容。且String也是final，不会被继承。

```
vmtool --action getInstances --className java.lang.String --limit -1
```
这样做的好处是可以使用字符串常量池去存字符串了，String也因为不可变可以做HashMap的默认key，用作缓存的hashcode，并且不可变对象直接线程安全了。

### final的不可变性
final可以用来修饰变量、方法、类，用来描述不可变。  
final修饰变量，意味变量一旦赋值则不能被修改。如果我们尝试对一个已经赋值过 final 的变量再次赋值，就会报编译错误。  
final修饰方法，任何继承类都不能重写该方法，不能被override。  
final修饰类，表示类不可被继承。  
一般来说，使用final可以控制影响范围，但是这对代码的扩展性会有影响，因此为了防止后续维护者有困惑，我们有必要说明原因。  

PS:对于上面的final修饰变量，一旦赋值不能被修改，对基础类型的是不能改了，但是对于对象、或者数组，是存的引用。修改对象、数组内容对引用是不会有影响的。
如果想做到，对象的不可变性，就要像上面的String一样，只提供一个初始化的方法去构造变量，内部无其他办法修改。并且这个变量是private的，无法被外部修改，于是相当于生成了就不可变。  


### 内部类
https://www.cnblogs.com/dolphin0520/p/3811445.html

### 泛型的使用

参考文档：  
https://blog.csdn.net/s10461/article/details/53941091
https://www.runoob.com/java/java-generics.html
https://www.liaoxuefeng.com/wiki/1252599548343744/1265102638843296