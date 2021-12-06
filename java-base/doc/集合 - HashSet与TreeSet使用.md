## 1.Set接口特点
1. Set 接口实例存储的是无序的，不重复的数据。List 接口实例存储的是有序的，可以重复的元素。

2. Set检索效率低下，删除和插入效率高，插入和删除不会引起元素位置改变 <实现类有HashSet,TreeSet>。

3. List和数组类似，可以动态增长，根据实际存储的数据的长度自动增长List的长度。查找元素效率高，插入删除效率低，因为会引起其他元素位置改变 <实现类有ArrayList,LinkedList,Vector> 。  

## 2.Set使用

hashSet - 底层基于hashmap实现，常用方法:
```text
private static final Object PRESENT = new Object();  //用来与重复对象映射的虚拟对象

public HashSet() { map = new HashMap<>();}    //无参构造函数，直接new一个hashma

public HashSet(int initialCapacity, float loadFactor) { map = new HashMap<>(initialCapacity, loadFactor);}

public boolean isEmpty() { return map.isEmpty();}

public boolean contains(Object o) { return map.containsKey(o);}

public boolean add(E e) { return map.put(e, PRESENT)==null; }

public boolean remove(Object o) { return map.remove(o)==PRESENT; }

public void clear() { map.clear();}
```
treeSet - 底层基于treemap实现，常用方法:
```text
private static final Object PRESENT = new Object();  //用来与重复对象映射的虚拟对象

TreeSet(NavigableMap<E,Object> m) { this.m = m; }    //底层基于NavigableMap实现，一种继承sortedmap的接口

public TreeSet() { this(new TreeMap<E,Object>()); }  //无参构造方法

public TreeSet(SortedSet<E> s) {
        this(s.comparator());
        addAll(s);
    }                                                //有参构造方法，需要传入对象支持comparator，才能做到排序

public int size() { return m.size(); }

public boolean isEmpty() { return m.isEmpty(); }

public boolean contains(Object o) { return m.containsKey(o); }

public boolean add(E e) { return m.put(e, PRESENT)==null; }  //treeSet也是一种Set,所以也不允许元素重复，添加时会计算PRESENT

public boolean remove(Object o) { return m.remove(o)==PRESENT; }

public void clear() { m.clear(); }
```


## 3.遇到的问题
[一次 HashSet 所引起的并发问题](https://crossoverjie.top/2018/11/08/java-senior/JVM-concurrent-HashSet-problem/)