## 1.jol简介
JOL的全称是Java Object Layout。是一个用来分析JVM中Object布局的小工具。包括Object在内存中的占用情况，实例对象的引用情况等等。

JOL可以在代码中使用，也可以独立的以命令行中运行。这里主要介绍在代码中使用JOL。使用JOL需要添加maven依赖：
``` xml
<dependency>
            <groupId>org.openjdk.jol</groupId>
            <artifactId>jol-core</artifactId>
            <version>0.10</version>
</dependency>
```

## 2.使用jol分析VM信息
```
VM.current().details()
```
输出 Objects are 8 bytes aligned，这意味着所有的对象分配的字节都是8的整数倍。  


## 3.使用jol分析类和对象（Class Instance）
在java中的对象，除了数组，其他对象的大小应该是固定的。以String为例：
ClassLayout.parseClass(String.class).toPrintable()
```txt
[main] INFO com.flydean.JolUsage - java.lang.String object internals:
 OFFSET  SIZE      TYPE DESCRIPTION                               VALUE
      0    12           (object header)                           N/A
     12     4    byte[] String.value                              N/A
     16     4       int String.hash                               N/A
     20     1      byte String.coder                              N/A
     21     1   boolean String.hashIsZero                         N/A
     22     2           (loss due to the next object alignment)
Instance size: 24 bytes
Space losses: 0 bytes internal + 2 bytes external = 2 bytes total
```  
OFFSET是偏移量，也就是到这个字段位置所占用的byte数，SIZE是后面类型的大小，TYPE是Class中定义的类型，DESCRIPTION是类型的描述，VALUE是TYPE在内存中的值。

分析下上面的输出，我们可以得出，String类中占用空间的有5部分，第一部分是对象头，占12个字节，第二部分是byte数组，占用4个字节，第三部分是int表示的hash值，占4个字节，第四部分是byte表示的coder，占1个字节，最后一个是boolean表示的hashIsZero，占1个字节，总共22个字节。但是JVM中对象内存的分配必须是8字节的整数倍，所以要补全2字节，最后String类的总大小是24字节。  

不同的对象其实偏移量不一样，比如Pod就是里面有4个object。并且其实存放了大量数据的对象，也是这个大小，因为对象大小不会变，object只是value值变了，存放对象在堆内的引用地址。  


## 4.使用jol分析数组大小
虽然对象的大小不变，但是数组大小肯定会变，只是引用地址不变。以string的byte[]为例，默认16字节，存放之后会变多。
log.info("{}",ClassLayout.parseInstance("www.flydean.com".getBytes()).toPrintable());
```
[main] INFO com.flydean.JolUsage - [B object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           22 13 07 00 (00100010 00010011 00000111 00000000) (463650)
     12     4        (object header)                           0f 00 00 00 (00001111 00000000 00000000 00000000) (15)
     16    15   byte [B.<elements>                             N/A
     31     1        (loss due to the next object alignment)
Instance size: 32 bytes
Space losses: 0 bytes internal + 1 bytes external = 1 bytes total

```

## 5.使用jol分析引用关系
上面使用ClassLayout只能分析对象大小，所以每次大小是相同的。所以要看不同的就一定要使用GraphLayout。 
```
HashMap hashMap= new HashMap();
hashMap.put("flydean","www.flydean.com");
log.info("{}", GraphLayout.parseInstance(hashMap).toPrintable());
[main] INFO com.flydean.JolUsage - java.util.HashMap@57d5872cd object externals:
          ADDRESS       SIZE TYPE                      PATH                           VALUE
        7875f9028         48 java.util.HashMap                                        (object)
        7875f9058         24 java.lang.String          .table[14].key                 (object)
        7875f9070         24 [B                        .table[14].key.value           [102, 108, 121, 100, 101, 97, 110]
        7875f9088         24 java.lang.String          .table[14].value               (object)
        7875f90a0         32 [B                        .table[14].value.value         [119, 119, 119, 46, 102, 108, 121, 100, 101, 97, 110, 46, 99, 111, 109]
        7875f90c0         80 [Ljava.util.HashMap$Node; .table                         [null, null, null, null, null, null, null, null, null, null, null, null, null, null, (object), null]
        7875f9110         32 java.util.HashMap$Node    .table[14]                     (object)

```
从结果我们可以看到HashMap本身是占用48字节的，它里面又引用了占用24字节的key和value。 

查看对象占用空间总大小：GraphLayout.parseInstance(obj).totalSize()

## 6.使用jol分析java对象锁
因为jol可以拿到对象的对象头信息，所以可以更加直观的观察对象锁的升级机制。  
从上面的分析可以很直接的看到，java对象在内存中的布局为对象头、实例数据、对齐填充。  
  对象头主要包含两部分数据：Markword、类型指针。
  Markword中主要包含哈希码（HashCode）、GC分代年龄、锁状态标志位、线程持有的锁、偏向线程ID等信息。这部分数据长度在32位和64位虚拟机中的长度为32bit和64bit。但是自从6 update 23开始在64位系统上会默认开启压缩指针。  
  ![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/jvm-markword.png)
  这里还涉及synchronized的一系列锁升级机制，以及jvm机制，这里只介绍部分tips：
  1.膨胀过程：无锁 001（锁对象初始化时）-> 偏向锁 101（有线程请求锁） -> 轻量级锁 00（多线程轻度竞争）-> 重量级锁 10（线程过多或长耗时操作，线程自旋过度消耗cpu）；  GC 11。
  2.jvm默认延时4s自动开启偏向锁（此时为匿名偏向锁，不指向任务线程），可通过-XX:BiasedLockingStartUpDelay=0取消延时；如果不要偏向锁，可通过-XX:-UseBiasedLocking = false来设置。  
  3.锁只能升级，不能降级；偏向锁可以被重置为无锁状态  
  4.锁对象头记录占用锁的线程信息，但不能主动释放，线程栈同时记录锁的使用信息，当有其他线程（T1）申请已经被占用的锁时，先根据锁对向的信息，找对应线程栈，若线程已结束，则锁对象先被置为无锁状态，再被T1线程占有后置为偏向锁；若线程位结束，则锁状态由当前偏向锁升级为轻量级锁。  
  5.偏向锁和轻量级锁在用户态维护，重量级锁需要切换到内核态(os)进行维护；
  
  以及注意我们看的时候32位对象头，jol打印的顺序和实际阅读的顺序是反的，所以按阅读顺序从高位到低位是反着的。最后锁的三位其实在展示的第一个8位中。
 

## 官方示例 
http://hg.openjdk.java.net/code-tools/jol/file/tip/jol-samples/src/main/java/org/openjdk/jol/samples/  




参考文档：
https://www.cnblogs.com/flydean/p/java-object-layout-jol.html
https://m.yisu.com/zixun/196553.html