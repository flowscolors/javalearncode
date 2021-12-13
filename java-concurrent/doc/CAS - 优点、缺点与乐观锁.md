

## 优点


使用：
ConcurrentHashMap 的 putVal 方法 ，使用compareAndSwapObject 
ConcurrentLinkedQueue 的 offer 方法，使用casNext方法



## 缺点
1.ABA问题 无法判断状态之间的变化，于是引入版本号，并且和状态合并到一个原子int值上
2.自旋过长 因为单次CAS不一定能执行成功，于是基本是配合while(true)循环，CAS如果一直操作不成功，CPU资源会一直被消耗
3.只能保证一个共享变量的原子操作。如果两个我们一遍会合并到一个原子int。如果多个对象则一般合成一个新的类，对其AtomicReference整体进行CAS操作。


## 原理

### 3.1 CAS方法

``` Java
public final native boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5);

public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);

public final native boolean compareAndSwapLong(Object var1, long var2, long var4, long var6);

```

对应汇编语言的CMPXCHG指令。

相应问题：

* ABA问题  -> 加版本号解决
* 自旋时，循环时间过长  -> 限制自旋次数解决
* 只能保证一个共享变量 -> 将多个共享变量和成一个，比如一个16位的数字，前3位，后13位各代表一个共享变量。