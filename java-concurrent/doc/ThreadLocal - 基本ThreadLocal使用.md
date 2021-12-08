

ThreadLocal，此类提供线程局部变量。1.2版本的Java中就有了，所以它并不能像1.5的AQS系列一样去做同步，而只是做一个线程独立变量的功能。
这些变量与普通变量不同，因为每个访问该变量的线程（通过其 * {@code get} 或 {@code set} 方法）都有自己的、独立初始化的变量副本。 

只要线程是活动的并且ThreadLocal实例是可访问的，每个线程都持有一个对它的线程局部变量副本的隐式引用；在一个线程消失后，它的所有线程本地实例的副本都会受到垃圾回收的影响（除非存在对这些副本的其他引用）

场景 - {@code ThreadLocal} 实例通常是希望将状态与线程相关联的类中的私有静态字段（例如，用户 ID 或事务 ID）:
1.官方例子。例如，下面的类生成每个线程的本地唯一标识符。线程的 id 在它第一次调用 {@code ThreadId.get()} 时被分配并且在后续调用中保持不变。

2.保存线程上下文信息，在任意需要的地方可以获取。比如存一串请求的id用ThreadLocal进行set，后续把请求串起来。否则Controller中的id不一定拿到固定的。
    比如Spring的事务管理，用ThreadLocal存储Connection，从而各个dao层可以获取同一Connection进行事务回滚、提交操作。
    
3.存线程安全的变量，避免某些情况需要考虑线程安全必须同步带来的性能损失


## ThreadLocal 内部方法
需要注意几点的是:
1.每个线程Thread中有一个ThreadLocalMap的对象threadLocals，但是该对象由ThreadLocal维护 ThreadLocal.ThreadLocalMap threadLocals = null; 完成线程线性探针哈希映射
所以ThreadLocal实际是map中节点的value值，可以说是最小单元了（毕竟它一个ThreadLocal只能存储一个Object对象），多个含ThreadLocal的值组成一个ThreadLocalMap，被一个线程指向。

2.ThreadLocalMap 是一个定制的哈希映射表，注意这是一个由数组实现的hash表，仅适用于维护线程本地值，不会在 ThreadLocal 类之外导出任何操作。该类是包私有的，但允许在类 Thread 中声明字段。
为了帮助处理非常大且长期存在的用法，哈希表条目使用 WeakReferences 作为键 key。然而，由于不使用引用队列，所以只有在表开始耗尽空间时才能保证删除陈旧的条目。

3.ThreadLocalMap 的条目扩展了 WeakReference，使用其主要 ref 字段作为键（它总是一个 ThreadLocal 对象）。请注意，空键（即 entry.get()  == null）意味着不再引用该键，因此可以从表中删除条目。此类条目在以下代码中被称为为“陈旧条目”

4.Entry其实就是继承了WeakReference，内部实际就用一个Object存了Thread的类型，存与此 ThreadLocal 关联的值。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/ThreadLocal-Entry.png)

ThreadLocal常用内部方法:
```text
    private final int threadLocalHashCode = nextHashCode();   // ThreadLocal 对象充当key，通过 threadLocalHashCode 搜索。这是一个自定义的哈希码（仅在 ThreadLocalMaps 中有用），它在相同线程使用连续构造的 ThreadLocals 的常见情况下消除了冲突，同时在不太常见的情况下保持良好行为
    
    private static AtomicInteger nextHashCode = new AtomicInteger();  //要给出的下一个哈希码。原子更新。从零开始。
    
    private static final int HASH_INCREMENT = 0x61c88647;   //连续生成的哈希码之间的差异 - 将隐式顺序线程本地 ID 转换为近乎最优的散列乘法哈希值，用于 2 次方大小的表。
    
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);      //每次用来生成下一位的hash码，返回nextHashCode
    }
    
    protected T initialValue() { return null; }             //返回此线程局部变量的当前线程的“初始值”。该方法将在线程第一次使用 get方法访问变量时被调用，如果程序员需要自己调用希望线程局部变量具有 null 以外的初始值，则必须将 ThreadLocal 子类化，并重写此方法。
    
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }
    
    public ThreadLocal() {}                                 //ThreadLocal构造方法 不会执行任何操作
    
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }                                      //返回此线程局部变量的当前线程副本中的值。如果变量没有当前线程的值，它首先被初始化为通过调用 {@link #initialValue} 方法返回的值。
    
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }                                //用于建立初始值的 set() 变体。使用private代替 set() 以防用户覆盖 set() 方法。
    
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }                              //将此线程局部变量的当前线程副本设置为指定值。大多数子类将不需要覆盖此方法，仅依靠 {@link #initialValue} 方法来设置线程局部变量的值。
    
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread());
         if (m != null)
             m.remove(this);
     }                           //删除此线程局部变量的当前线程值。如果此线程局部变量随后被调用由当前线程将通过调用其 {@link #initialValue} 方法重新初始化

    static class ThreadLocalMap {

        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        private static final int INITIAL_CAPACITY = 16;  //ThreadLocalMap初始容量16，必须是2的次幂

        private Entry[] table;     //Entry数组，需要调整大小。table.length 必须始终是 2 的幂

        private int size = 0;      //Entry数组中的条目个数

        private int threshold;    //要调整大小的下一个大小值。 默认为 0
    
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {···}  //构造一个最初包含 (firstKey, firstValue) 的新映射。 ThreadLocalMaps 是惰性构建的，因此我们只在至少有一个条目要放入时才创建一个。
    
        private ThreadLocalMap(ThreadLocalMap parentMap) {···}   //从给定的父映射构造一个包含所有可继承线程局部变量的新映射。仅由 createInheritedMap 调用。
    
        private Entry getEntry(ThreadLocal<?> key) {···}        //Entry数组中获取与key关联的条目。这个方法本身只处理快速路径。
    
        private void set(ThreadLocal<?> key, Object value) {···}  //往Entry数组中set (key,value)键值对。
        
        ···
        }
```


## ThreadLocal特点
1.ThreadLocal无法解决共享变量的更新问题。所以ThreadLocal变量建议使用static修饰，这个变量是针对一个线程内所有操作共享的，


2.对象弱引用的GC泄漏问题。
由于大多数情况下，我们使用线程池的场景，线程的生命周期很长，如果我们往ThreadLocal里面set了很大很大的Object对象，虽然set、get等等方法在特定的条件会调用进行额外的清理。
但是ThreadLocal被垃圾回收后，在ThreadLocalMap里对应的Entry的键值会变成null，但是后续在也没有操作set、get等方法了。

所以最佳实践是在不使用这个ThreadLocal对象时，主动调用remove方法进行清理。

结合上面两点，最合适的使用方法是:
```text
public class ThreadLocalTest {
    
    private static ThreadLocal<> threadlocal ;
    
    try {
        // 其它业务逻辑
    } finally {
        threadLocal对象.remove();
    }
}   
```


## 常用问题
1. SimpleDateFormat是线程不安全的类，一般不要定义为static变量，每次使用时new则可规避问题。如果定义为static，则必须加锁，或者使用DataUtils工具类。

2.ThreadLocal中使用了弱引入，导致Entry中键值对可能会被回收，为了避免内存泄漏，需要在finally中主动调用remove操作。

