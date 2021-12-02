## 1.Set接口特点
1. Set 接口实例存储的是无序的，不重复的数据。List 接口实例存储的是有序的，可以重复的元素。

2. Set检索效率低下，删除和插入效率高，插入和删除不会引起元素位置改变 <实现类有HashSet,TreeSet>。

3. List和数组类似，可以动态增长，根据实际存储的数据的长度自动增长List的长度。查找元素效率高，插入删除效率低，因为会引起其他元素位置改变 <实现类有ArrayList,LinkedList,Vector> 。  

## 2.Set使用



## 3.遇到的问题
[一次 HashSet 所引起的并发问题](https://crossoverjie.top/2018/11/08/java-senior/JVM-concurrent-HashSet-problem/)