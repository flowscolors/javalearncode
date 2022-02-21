
## 链表
常见单项链表的节点的数据结构。

```java
public class ListNode {
    public int val;
    public ListNode next;
    
    public ListNode(int val){
        this.val = val;
    }
}
```
### 常见解法
1.双指针。
    前后双指针。
    快慢双指针。

2.反转链表。经典基础模板，是很多其他题的前置步骤。

```text
public ListNode reverseList (ListNode head){
    ListNode pre = null;
    ListNode cur = head;
    while(cur!=null){
        ListNode next = cur.next;
        cur.next = pre;
        pre = cur;
        cur = next;
    }
    return pre;
} 
```



## 哈希表
哈希表是一种常见数据结构，理论上插入、删除、查询都是O(1)。Java中有hashmap、hashset两种类型，分别用来存kv值和单独存一个值。
哈希表的使用前提是待存入元素要有一个能计算自己哈希值的函数。而Java中的所有类型都继承了Object
HashSet 常用方法
add()        在HashSet中添加一个元素  
contains()   判断HashSet中是否包含元素
remove()     在HashSet中remove一个元素
size()       返回HashSet中元素个数。

HashMap常用方法
put()       在HashMap中添加一组映射，如果key不存在，则添加。如果存在则更新。
get()       如果key存在，则返回vaule
remove()    删除某个key
replace()   修改某个key的value
containsKey()  判断HashMap中是否包含某个key  

常见面试题：
1.使用HashMap实现一个LRU （最近最少使用缓存）
2.使用数组模拟哈希表，如果哈希表的键取值范围固定，则可以使用数组模拟哈希表。比如字符串的26字母。

