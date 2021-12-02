

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
平常我们执行一个 Thread 线程：
```text
new Thread(new Runnable() {
  @Override
  public void run() {
      System.out.println("xxxx");
  }
}).start();
```
如果用 lambda 会非常简洁，一行代码搞定。
```text
 new Thread(()-> System.out.println("xxx")).start();
```
所以在某些场景下使用 lambda 表达式可以减少 java 中一些冗长的代码，增加代码的优雅性。

## Lambda表达式语法
(parameters) -> expression
这种语法函数体是一个表达式, 表达式后面不需要分号, 不需要带大括号, 例 (x) -> "TEST"

(parameters) -> {statements;}
这种语法的函数体是语句, 语句后面需要带分号, 且需要带大括号, 例 (x) -> {return "TEST";}

## 