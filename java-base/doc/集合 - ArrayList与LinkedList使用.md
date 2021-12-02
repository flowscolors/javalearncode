## 1.


### 1.Arraylist、LinkedList初始化、扩容策略
* ArrayList 初始化大小10(初始0)，对象数组elementData被transient 修饰。如果数组满了，扩容按原长度的1.5倍扩。  
  并且ArrayList的插入可以插任一位置，或者尾插，默认尾插，后者性能更好。支持快速随机访问。
* LinkedList，初始化大小0，链表节点为Node,有element、next、prev，并且有了特点的first、last两个Node。   
  不支持快速随机访问、LinkedList插入也可以插任意位置和尾插，默认尾插。删除要先循环找到删除元素。遍历也是要循环，但是iterator 可以基于缓存直接拿到元素。

参考文档：  
https://www.cnblogs.com/zeroingToOne/p/9522814.html
https://www.cnblogs.com/kuoAT/p/6771653.html

### 2.ArrayList与LinkedList区别
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
