

ConcurrentHashMap,一个哈希表支持检索的完全并发性和更新的高预期并发性。该类遵循与 java.util.Hashtable 相同的功能规范，并且包括与 Hashtable 的每个方法对应的方法版本。
然而，即使所有操作都是线程安全的，检索操作不需要锁定，并且不支持任何以某种方式锁定整个表阻止所有访问。在依赖其线程安全但不依赖于其同步细节的程序中，此类完全可与 Hashtable 互操作。
 
对于检索操作 get 方法一般不会阻塞，因此可能与更新操作 put 和 remove 重叠。检索反映了最近完成更新操作的结果。 （更正式地说，对给定键的更新操作与报告更新值的键的任何（非空）检索具有 happens-before 关系。）
对于聚合操作 putAll 和 clear，并发检索可能仅反映某些条目的插入或删除。类似地，Iterators、Spliterators 和 Enumerations 返回反映哈希表在迭代器/枚举创建时或之后的某个时刻的状态的元素。他们不会抛出{@link * java.util.ConcurrentModificationException ConcurrentModificationException}。
然而，迭代器被设计为一次只能被一个线程使用。请记住，包括 * {@code size}、{@code isEmpty} 和 {@code containsValue} 在内的聚合状态方法的结果通常只有当Map没有在其他线程中进行并发更新时才有用。 * 否则，这些方法的结果反映了瞬态 * 可能足以用于监视或估计目的，但不适用于程序控制。

因为调用了某些并发操作的方法，不允许key、value为null值。put、replace等更新操作中使用了synchronized锁。  

这个哈希表的主要设计目标是保持并发可读性（通常是方法 get()，但也包含迭代器和相关方法）同时最小化更新争用。
次要目标是保持与 java.util.HashMap 相同或更好的空间消耗，并支持许多线程对空表的高初始插入率。

## ConcurrentHashMap内部方法
java.util.concurrent.ConcurrentHashMap内部常用方法:
```text
public class ConcurrentHashMap<K,V> extends AbstractMap<K,V>
    implements ConcurrentMap<K,V>, Serializable {
        
    private static final int MAXIMUM_CAPACITY = 1 << 30;   //最大可能的表容量。该值必须正好是 1<<30 以保持在 Java 数组分配和索引两个表大小的幂的范围内，并且进一步需要 因为 32 位哈希字段的前两位用于控制目的。
    
    private static final int DEFAULT_CAPACITY = 16;

    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    private static final float LOAD_FACTOR = 0.75f;
    
    static final int TREEIFY_THRESHOLD = 8;
    
    static final int UNTREEIFY_THRESHOLD = 6;
    
    static final int MIN_TREEIFY_CAPACITY = 64;
    
    private static final int MIN_TRANSFER_STRIDE = 16;
    
    public ConcurrentHashMap() {
    }
    
    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException();
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }
}
    



```


## ConcurrentHashMap特点
1.8以后使用节点锁代替1.7的分段锁操作。



## 一些坑

1.对于ConcurrentHashMap的方法，当 computeIfAbsent 的时候，里面还有一个 computeIfAbsent。并且这两个 computeIfAbsent 它们的 key 对应的 hashCode 是一样的。

这时候会第二个进入computeIfAbsent的对象就进入死循环，无法出来。此bug在jdk 1.9被修复。对于1.85版本的jdk需要外部进行判断后调用该方法。

先调用了 get 方法，如果返回为 null，则调用 putIfAbsent 方法，这样就能实现和之前一样的效果了。

2.CHM 本身一定是线程安全的。但是，如果你使用不当还是有可能会出现线程不安全的情况。

虽然 ConcurrentHashMap 是线程安全的，但是假设如果一个线程 put，一个线程 get，在某些代码的场景里面是不允许的，此时就需要在你自己的逻辑里使用synchronized 把几个操作封装成一个原子操作。
因为外部的逻辑可能需要的是先get再set那就有问题了，因为这两个都是原子操作，类似Redis的set、get，两个合并起来的原子操作是incr。

