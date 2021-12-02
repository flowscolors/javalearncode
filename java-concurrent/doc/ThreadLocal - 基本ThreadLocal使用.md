

ThreadLocal，一种特殊变量

场景:
1.保存线程上下文信息，在任意需要的地方可以获取

2.存线程安全的变量，避免某些情况需要考虑线程安全必须同步带来的性能损失


## ThreadLocal 内部方法









## ThreadLocal特点
1.ThreadLocal无法解决共享变量的更新问题。





## 常用问题
1. SimpleDateFormat是线程不安全的类，一般不要定义为static变量，每次使用时new则可规避问题。如果定义为static，则必须加锁，或者使用DataUtils工具类。

2.ThreadLocal中使用了弱引入，导致Entry中键值对可能会被回收，为了避免内存泄漏，需要在finally中主动调用remove操作。

