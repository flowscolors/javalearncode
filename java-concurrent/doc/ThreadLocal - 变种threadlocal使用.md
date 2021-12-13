
### InheritableThreadLocal  
解决子线程获取父线程变量的问题。
> 同一个 ThreadLocal 变量在父线程中被设置后, 在子线程中使用 ThreadLocal.get.() 方法是无法获取到得的, 因为两者是不同的线程。但是InheritableThreadLocal 就可以实现，它继承自 ThreadLocal, 允许子线程访问父线程中设置的本地变量, 使用方法和 ThreadLocal 一样。
>
>在项目中使用 InheritableThreadLocal 类比较多的一种情况是在输出日志的时候获取用于链路跟踪的 traceId, 方便日志排查, 在没有使用 InheritableThreadLocal 前使用的是在线程的构造方法中传入这个 traceId, 后来了解了 InheritableThreadLocal 类后就直接使用 InheritableThreadLocal 类保存 traceId, 相对于自定义一个类去继承 Thread 类并在类中定义一个变量保存 traceId 的值就方便很多了.
>
>InheritableThreadLocal 和 ThreadLocal 不同的是在创建线程的时候, 会将当前线程中 inheritableThreadLocals 变量中的值拷贝一份到被创建线程的 inheritableThreadLocals 变量中

InternalThreadLocal 是 ThreadLocal 的一个变种，当配合 InternalThread 使用时，具有比普通 Thread 更高的访问性能。

并且InternalThread 的内部使用的是数组，通过下标定位，非常的快。如果遇得扩容，直接数组扩大一倍，完事。

而 ThreadLocal 的内部使用的是 hashCode 去获取值，多了一步计算的过程，而且用 hashCode 必然会遇到 hash 冲突的场景，ThreadLocal 还得去解决 hash 冲突，如果遇到扩容，扩容之后还得 rehash ,这可不得慢吗？

### TransmittableThreadLocal


### fastThreadLocal




参考文档：  
https://zhuanlan.zhihu.com/p/266744246
http://antsnote.club/2019/06/19/Java-%E5%A4%9A%E7%BA%BF%E7%A8%8B%E4%B9%8BInheritableThreadLocal/  
https://mp.weixin.qq.com/s?__biz=MzU4MTc3NjQ3Mg==&mid=2247483699&idx=1&sn=ae5c62f6930ca3054135b96cfe24a3f7&chksm=fd432404ca34ad124ca32b7d255a5fc8245fee0224ef68a9aa5468a1267e966448231215032b&token=391257983&lang=zh_CN#rd
https://mp.weixin.qq.com/s?__biz=MzU4MTc3NjQ3Mg==&mid=2247483702&idx=1&sn=4e3a63b83c3727198db6d0df952d1e2d&chksm=fd432401ca34ad17f03774fcb7877bcb826a52a11dfb4e57c00aa0ad0c95b99c2e96c5742b1e&token=391257983&lang=zh_CN#rd
