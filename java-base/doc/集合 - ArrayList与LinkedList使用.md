


### 1.Arraylist、LinkedList初始化、扩容策略
* ArrayList 初始化大小10(初始0)，对象数组elementData被transient 修饰。如果数组满了，扩容按原长度的1.5倍扩。  
  并且ArrayList的插入可以插任一位置，或者尾插，默认尾插，后者性能更好。支持快速随机访问。
* LinkedList，初始化大小0，链表节点为Node,有element、next、prev，并且有了特点的first、last两个Node。   
  不支持快速随机访问、LinkedList插入也可以插任意位置和尾插，默认尾插。删除要先循环找到删除元素。遍历也是要循环，但是iterator 可以基于缓存直接拿到元素。

参考文档：  
https://www.cnblogs.com/zeroingToOne/p/9522814.html
https://www.cnblogs.com/kuoAT/p/6771653.html


### 1.ArrayList内部方法
ArrayList常用方法：
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
    private static final int DEFAULT_CAPACITY = 10;               //默认长度 10
    
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8; //最大长度2^30-8

    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}; //默认初始化为空数组
    
    private int size;
    
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;  //无参默认初始化为空数组
    }

    //初始化方法 
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }                                                            
 
    //扩容方法
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }                                                        
    
    //add操作 添加之前先检查是否需要扩容
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!! size作为数组下标从0开始加
        elementData[size++] = e;
        return true;
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {  //如果初始化的数组是空数组，把minCapacity置为10，并不是把数组长度置为10
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);  
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code  数组放不下的时候，扩容grow()
        if (minCapacity - elementData.length > 0)   //当首次放到10的时候触发扩容
            grow(minCapacity);
    }
    
    //remove操作  删除后位置置为null，让元素被GC
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }

    //for each 使用函数式接口Consumer实现，类似的有sort方法，基于函数式接口Comparator实现。
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }
}
```

### 2.LinkedList内部方法
LinkedList常用方法：
并且LinkedList既有队列的peek、poll，又有堆栈的pop、push。并且LinkedList是有序的，按照放入顺序排序。  
```java
public class LinkedList<E>
    extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    transient int size = 0;
    
    public LinkedList() {
    }
    
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }
    
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
    
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
    
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

}
```



### 2.ArrayList与LinkedList性能区别
* 一般认为ArrayList因为基于数组实现，查询快，增删慢；LinkedList基于双向链表实现，查询慢，增删快。  具体使用可以参见demo。  
* 当然具体的场景下结果可能不同，因为实际效率与元素在集合中位置是相关的。比如插入、删除从头做则Arraylist慢，而中间、尾部做插入删除则Arraylist快。
* Java 对于集合数据的遍历提供了几种不同的方式, 普通的 for 循环, 迭代器 Iterator 和 forEach得到结果也不相同。比如Iterator会进行缓存操作，不用每次都从头结点开始访问，会缓存当前结点的前后结点。
* ArrayList新增主要是扩容，扩容需要移动元素，  移动元素使用了System.arraycopy函数，而这个函数的效率是很高的，因为它是一个native方法，可能直接操作了内存。LinkedList新增主要在new 对象，并且前后指针的指向。

参考文档：  
http://learn.lianglianglee.com/%E4%B8%93%E6%A0%8F/Java%E5%B9%B6%E5%8F%91%E7%BC%96%E7%A8%8B%E5%AE%9E%E6%88%98/05%20%20ArrayList%E8%BF%98%E6%98%AFLinkedList%EF%BC%9F%E4%BD%BF%E7%94%A8%E4%B8%8D%E5%BD%93%E6%80%A7%E8%83%BD%E5%B7%AE%E5%8D%83%E5%80%8D.md  
http://antsnote.club/2018/10/20/Java-ArrayList%E5%92%8CLinkedList%E5%BE%AA%E7%8E%AF%E9%81%8D%E5%8E%86%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95/ 
https://www.cnblogs.com/whoislcj/p/6508851.html  
https://www.chuckfang.com/2019/06/21/arraylist-and-linkedlist/  
https://moe.best/java/java-array-and-linked.html

### 3.ArrayList、LinkedList线程不安全，并发问题

ArrayList线程不安全:

场景一:多线程调用add()方法。此时可能会触发两个问题。注意这里的ArrayList是作为共享变量出现。

1.当此时是需要进行数组扩容的临界点时，如果有两个线程同时进行插入操作，可能会导致数组下标越界异常。
在数组容量为9的时候同时两个线程执行add()，都先判断加完是10，不需要扩容。于是 elementData[size++] = e;实际前一个做完后，size变为10，于是后一个就变为往一个长度为10的数组的位置11放值，数组越界。

2.由于往数组中添加元素不是原子操作，所以可能会导致元素覆盖的情况出现。
同样也是elementData[size++] = e这个操作不是一个原子操作，当线程1刚把值放进去，还未执行size++时被CPU挂起；线程2就会把值也放到这个位置，于是后面的值就覆盖了前面的值。

稳定复现可以重写ArrayList方法，并且以上两个问题都可以在add()方法前加synchronized 解决。


ArrayList一定是线程不安全的吗？
场景一：使用场景只是只读，那就是线程安全的。大家都是来读。
场景二：当 ArrayList 是方法内的局部变量时，比如@Service中的某个方法，是没有线程安全的问题的。


参考文档： 
https://mp.weixin.qq.com/s/MNeD5Idjqgw87wI0reqrvw