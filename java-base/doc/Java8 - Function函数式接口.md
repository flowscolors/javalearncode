
函数式接口，是指定义了一个抽象方法的接口。
函数式接口在Java中是指：有且仅有一个抽象方法的接口。重要。
在Java8中接口已经可以有默认方式实现了，但是即使有了默认方法实现，只要有一个抽象方法就还是函数式接口。

一般使用方法是自己定义或者使用java默认的函数式接口 @FunctionalInterface标识，并且只包含一个抽象方法的接口 + 使用Lambda表达式实现该接口的实现
```text
@FunctionalInterface
interface GreetingService 
{
    void sayMessage(String message);
}

GreetingService greetService1 = message -> System.out.println("Hello " + message);
```

## 方法引用
方法引用分为三种，方法引用通过一对双冒号:: 来表示，方法引用是一种函数式接口的另一种书写方式：
* 静态方法引用，通过类名::静态方法名。如Interger::parseInt
* 实例方法引用，通过实例对象::实例方法名。如str::substring
* 构造方法引用，通过类名::new。如User::new

通过方法引用，可以将方法的引用赋值给一个变量，通过赋值给Function，说明方法引用也是一种函数式接口的书写方式。

Lambda表达式也是一种函数式接口，Lambda表达式一般自己提供方法体，方法引用则直接使用现有的方法。并且Lambda表达式要求接口仅有一个抽象方法。

## Java 8 中的新增的函数式接口
Java 8 的 java.util.function 包中引入了一些新的函数式接口, 如 Predicate, Consumer 和 Function, 现在分别介绍这几个接口

### Predicate
Predicate 接口里面有一个返回 boolean 类型的 test 抽象方法和其他几个默认实现方法, 当调用 test 方法时会执行传递进来的
Lambad 表达式的方法体里面的代码并返回一个 boolean 对象, 一般用于需要返回一个 boolean 类型的 Lambda 表达式,可以接用Predicate实现一个filter功能的方法。  
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }
}
```
Predicate 接口还默认实现了 and, or 和 negate 方法, 这些方法可以和 test 组合使用, 例如将上面的 filter 方法改成既要满足 Lambda 表达式的筛选条件, 又要满足元素的 name 是 L 的元素
```text
private static List<DataNode> filter(List<DataNode> sourceDataList, Predicate<DataNode> predicate) {
    List<DataNode> returnDataList = new ArrayList<>();
    sourceDataList.forEach(dataNode -> {
        if (predicate.and(dataNodec -> {
            log.info("AND    = " + dataNodec.toString());
            return dataNodec.getName().equals("L");
        }).test(dataNode)) {
            log.info("TEST   = " + dataNode.toString());
            returnDataList.add(dataNode);
        }
    });
    return returnDataList;
}
```


### Comsumer
Consumer 接口里面定义了一个 accept 的抽象方法, 接收一个泛型参数, 返回一个 void 类型, 另外一个默认的实现方法 
addThen 可以在 accept 方法执行后再次对数据进行一次操作, 如果仅需要对某个数据进行操作, 可以使用次接口, 类似于 Stream API 中的 forEach 方法。
```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
    
    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
```

### Function
Function 接口定义了一个 apply 的接口, 接收一个泛型类型 T, 并返回一个泛型 R, 其中还有两个默认实现的方法, compose 和 andThen, 
这两个方法分别在 apply 方法执行前调用和执行后再次对数据进行一次映射, 如果需要将某个类型映射成其他的类型, 可以使用该接口, 和 Stream API 中的 map 方法类似。
```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);

    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
```

### Supplier
Supplier 接口只有一个抽象的 get 方法, 不接受任何参数, 返回一个泛型 T 参数, 比较简单就不举例了, 源码如下:
```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```

### 其他的接口
上面说到的 Predicate, Comsumer 和 Function 是 java.util.function 提供的接口, 除了这些接口外, 还提供了一些其他的接口, 但都是和 Predicate, Comsumer 和 Function 相关的接口, 主要分成以下两类:

* BiXXX 类型的接口, 如 BiFunction 接口, 这类接口和 Predicate, Comsumer 和 Function 接口功能一样, 不同的是这类接口有两个入参
* ObjXXXYYY 类型接口, 如 ObjIntFunction 接口, 这类接口有两个入参, 只是第一个参数是对象类型, 第二个参数是基础数据类型 (int, long, double)
* XXXToYYYFunction, 如 LongToIntFunction 接口, 基础类型 long 映射 int 类型使用
* 最后还有一类是为了不让处理数据时对基础类型进行自动的拆箱和装箱操作影响性能而创建的诸如 ToIntFunction 等接口

### JDK8以前的函数式接口

java.lang.Runnable
java.util.concurrent.Callable
java.security.PrivilegedAction
java.util.Comparator
java.io.FileFilter
java.nio.file.PathMatcher
java.lang.reflect.InvocationHandler
java.beans.PropertyChangeListener
java.awt.event.ActionListener
javax.swing.event.ChangeListener


参考文档：  
https://www.runoob.com/java/java8-functional-interfaces.html
http://antsnote.club/2018/10/18/Java-Java8%E5%87%BD%E6%95%B0%E5%BC%8F%E6%8E%A5%E5%8F%A3/