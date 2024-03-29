


## Hashmap底层实现
JDK 1.8中使用位桶+链表+红黑树

HashMap常用方法:
```java
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
    
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    
    static final int MAXIMUM_CAPACITY = 1 << 30;
    
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    static final int TREEIFY_THRESHOLD = 8;
    
    static final int UNTREEIFY_THRESHOLD = 6;
    
    static final int MIN_TREEIFY_CAPACITY = 64;
    
    //计算hash 并将散列从高位传导到低位
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
    
    //初始化方法，传入初始化查长度与散列因子
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }
    
    //放入单个元素
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K,V> e; K k;
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
    
    //扩容操作  扩容时新的长度会被用来计算位与操作确定元素在数组中新的位置。所以元素要么保持不变，要么移动2次幂个位置。
    final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

}
```

TreeMap常用方法
```text
private final Comparator<? super K> comparator;           //树中元素的比较器

private transient int size = 0;                           //树中元素个数

private transient int modCount = 0;                      //树的修改次数

public TreeMap() { comparator = null; }                   //无参构造方法，直接把比较器置为null

public TreeMap(Comparator<? super K> comparator) { this.comparator = comparator; }

public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}

public boolean containsKey(Object key) { return getEntry(key) != null;}

public V get(Object key) {
    Entry<K,V> p = getEntry(key);
    return (p==null ? null : p.value);
}

public V put(K key, V value) {···}

public V remove(Object key) {···}
```


## HashMap初始化长度、扩容和缩容
初始化长度length(默认值是16)，Load factor为负载因子(默认值是0.75)

冲突：当两个对象的hashcode计算值相同，就产生了hash冲突，hashmap会先把冲突对象挂到一个链表上，当链表长度到8时且数组长度大于64时，会树化，红黑树。
这里的链表阶段中，新的冲突对象插入链表在JDK7里是头插法，在JDK8里是尾插法。

扩容:put的时候触发扩容，当  ++size > threshold （threshold 为数组长度 * 散列因子） 时触发扩容，直接*2。
因为实际数组就是hash表，如果冲突过多会影响存取效率，所以扩容是当冲突过多时需要实现的。


缩容: 无。 也正是因为Java的hashmap无缩容的机制，所以可能出现大量数据入队出队后，留下很多空的node节点。

PS:java的map不允许，go的map有这种操作。但是其实也是伪操作，实际要缩减还是要手动缩容。

参考文档：  
https://www.shuzhiduo.com/A/rV57qYAGzP/
https://www.zhihu.com/question/366679456

## hashmap并发的问题
1.扩容时出现著名的环形链表异常，该问题在JDK1.8由头插改为尾插解决。1.7的头插在重新hash时会导致死循环。


2.并发情况下脏读脏写。


3.hashmap一定是线程不安全的吗？虽然本身是一个线程不安全的容器，但是如果使用场景只是只读，那就是线程安全的。


## 常见面试题
Q:hashmap如何解决hash冲突，为什么hashmap中的链表需要转成红黑树？
A:hashmap是使用链地址法解决，链表转红黑树是为了提高存取效率，因为理想的hash表存、取、读是O(1)。hashmap的树化需要数组长度到64，并且链表长度大于8.
实际Java中还是有其他的hash表使用其他方式解决hash冲突，比如ThreadLocal中的ThreadLocalMap就是一个hash表，其中使用重hash的方法解决冲突。

Q：hashmap什么时候会触发扩容？
A：需要扩容是因为当存取元素过多，会影响存取效率。触发是++size > threadsold时会触发，即大于 数组长度 * 负载因子。

Q:jdk1.8之前并发操作hashmap时为什么会有死循环的问题？
A:jdk1.8以前的hashmap的冲突解决链地址是用头插法，并发执行+重hash，会导致死循环。1.8以后使用尾插法规避了这个问题。

参考文档： https://tech.meituan.com/2016/06/24/java-hashmap.html

Q:hashmap扩容时每个entry需要再计算一次hash吗？
A:需要，扩容了都需要再进行重hash。但是因为长度是2次幂，所以和原有位置相比，要么是不变，要么是位移 > 2。
注意扩容是一个特别耗性能的操作，所以当程序员在使用HashMap的时候，估算map的大小，初始化的时候给一个大致的数值，避免map进行频繁的扩容。

Q:hashmap的数组长度为什么要保证是2的幂？
A:为了使用位运算来计算位移，方便扩容和重hash的计算。

Q：如何用LinkedHashMap实现LRU？
A:LinkedHashMap是HashMap的子类,在原有HashMap数据结构的基础上,它还维护着一个双向链表链接所有entry,这个链表定义了迭代顺序，通常是数据插入的顺序。
LinkedHashMap单独维护了一个双向链表，用于记录元素插入的顺序。内含accessOrder属性则指明了进行遍历时是按照什么顺序进行访问,我们可以通过它的构造方法自己指定顺序。
```shell script
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    
  private int cacheSize;
  
  public LRUCache(int cacheSize) {
      super(16,0.75f,true);
      this.cacheSize = cacheSize;
  }

  /**
   * 判断元素个数是否超过缓存容量，重写即可实现移除最旧的逻辑。
   */
  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > cacheSize;
  }
}
```

Q:如何用TreeMap实现一致性hash？
A:简单的分区取余不满足分布式随时拓展的需要,保证可拓展性就是一致性hash。当然实际分片的时候还是需要分布式的hash操作。

Q:TreeMap的key对象为什么必须要实现Compare接口
A:因为TreeMap需要保证对象的顺序，而对对象的顺序比较就需要对象自己实现Compare接口。