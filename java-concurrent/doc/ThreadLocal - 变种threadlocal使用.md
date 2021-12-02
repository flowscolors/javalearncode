
## 2.ThreadLocal变体
* InheritableThreadLocal  
> 同一个 ThreadLocal 变量在父线程中被设置后, 在子线程中使用 ThreadLocal.get.() 方法是无法获取到得的, 因为两者是不同的线程。但是InheritableThreadLocal 继承自 ThreadLocal, 允许子线程访问父线程中设置的本地变量, 使用方法和 ThreadLocal 一样。
>
>在项目中使用 InheritableThreadLocal 类比较多的一种情况是在输出日志的时候获取用于链路跟踪的 traceId, 方便日志排查, 在没有使用 InheritableThreadLocal 前使用的是在线程的构造方法中传入这个 traceId, 后来了解了 InheritableThreadLocal 类后就直接使用 InheritableThreadLocal 类保存 traceId, 相对于自定义一个类去继承 Thread 类并在类中定义一个变量保存 traceId 的值就方便很多了.
>
>InheritableThreadLocal 和 ThreadLocal 不同的是在创建线程的时候, 会将当前线程中 inheritableThreadLocals 变量中的值拷贝一份到被创建线程的 inheritableThreadLocals 变量中
>
* fastThreadLocal

参考文档：  
http://antsnote.club/2019/06/19/Java-%E5%A4%9A%E7%BA%BF%E7%A8%8B%E4%B9%8BInheritableThreadLocal/  
