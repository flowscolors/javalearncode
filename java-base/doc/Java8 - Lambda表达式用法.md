

Java中的 lambda 表达式实质上是一个匿名方法，但该方法并非独立执行，而是用于实现由函数式接口定义的唯一抽象方法。

Lambda 表达式是一种可传递匿名表达式的一种方式, 可以作为参数传递或者存储在变量中, 使用 Lambda 可以让行为参数化更加的灵活

先来看一个例子:  
使用 lambda 表达式时，会创建实现了函数式接口的一个匿名类实例。可以说Lambda表达式是函数式接口的一个具体实例。实际我们也是在函数式接口上使用Lambda表达式。  
如 Java8 中的线程 Runnable 类实现了函数接口：@FunctionalInterface。
```text
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}
```

平常我们执行一个 Thread 线程，这里的@Override了一个Runnable类，实际是传统匿名类的使用，不在需要在外部定义一个Runnable的实现类：
```text
new Thread(new Runnable() {
  @Override
  public void run() {
      System.out.println("xxxx");
  }
}).start();
```

如果用 lambda 会非常简洁，一行代码搞定。Lambda是一个匿名函数，可以理解为一段可以传递的代码。
```text
 new Thread(()-> System.out.println("xxx")).start();
```
所以在某些场景下使用 lambda 表达式可以减少 java 中一些冗长的代码，增加代码的优雅性。

## Lambda表达式语法
(parameters) -> expression
这种语法函数体是一个表达式, 表达式后面不需要分号, 不需要带大括号, 例 (x) -> "TEST"

(parameters) -> {statements;}
这种语法的函数体是语句, 语句后面需要带分号, 且需要带大括号, 例 (x) -> {return "TEST";}

在Lambda标准格式的基础上，使用省略写法的规则为：

小括号内参数的类型可以省略
如果小括号内有且仅有一个参数，则小括号可以省略
如果大括号内有且仅有一个语句，可以同时省略大括号、return关键字及语句分号
(int a) -> { return new Person(); }   省略后 a -> new Person;

## 使用前提
Lambda的语法非常简洁，但是Lambda表达式不是随便使用的，使用时有几个条件要特别注意：

1.方法的参数或局部变量类型必须为接口才能使用Lambda。

2.接口中有且仅有一个抽象方法。如Runnable接口只有一个 public abstract void run();抽象方法，否则Lambda并不知道自己要执行哪个抽象方法。  
只有确保接口中有且仅有一个抽象方法，Java中的Lambda才能顺利地进行推导。


## Lambda和匿名内部类对比
也因此Lambda和匿名内部类在使用上的区别：
1.所需类型不一样。匿名内部类，需要的类型可以是类、抽象类、接口。而Lambda表达式必须是只有一个抽象方法的接口。

2.实现原理不同。匿名内部类是在编译后会形成class Lambda表达式是在程序运行的时候动态生成class