
 volatile，它是 Java 中的一个关键字，是一种同步机制。当某个变量是共享变量，且这个变量是被 volatile 修饰的，那么在修改了这个变量的值之后，再读取该变量的值时，可以保证获取到的是修改后的最新的值，而不是过期的值。  
 但是它只保证get的时候是准确的，不保证set的时候是准确的。  

相比于 synchronized 或者 Lock，volatile 是更轻量的，因为使用 volatile 不会发生上下文切换等开销很大的情况，不会让线程阻塞。但正是由于它的开销相对比较小，所以它的效果，也就是能力，相对也小一些。

## 1.作用
保证可见性、有序性，不保证原子性。内存屏障可以保证新的主存刷新回时，通知其他CPU缓存的变量失效，但是如果其他CPU的变量已经进了寄存器就没办法了。所以多线程i++会出现问题。  

* 防止指令重排 如a = new A（）。就是三步 堆里面创建实际的对象，把对象进行初始化设置，把对象赋给引用。如果3提到2前就会导致未执行构造函数的方法就被返回，可能调用时就没有赋初值了。

* 保证单一操作的可见性  如布尔标志位的使用

* 作为触发器，保证其他变量可见性，让一个线程内修改的变量可以被其他线程读取到，从而控制别的线程的行为  A线程去改volatile修饰的变量，B线程一直拿该变量while(true)，这样A线程改的时候，就能触发B线程的操作。

相当于保证了happen-before。如何实现happen-before。首先JVM层肯定是基于LoadStore内存屏障，再底层基于内存屏障，java会调用把内存屏障的相应的机器码插入。字节码层面添加ACC_VOLATITLE

1.底层是lock指令。实际是用lock指令阻塞请求，变量写回主存且使其他CPU缓存的变量无效。因为lock指令前缀具有内存屏障的语意且有时候比mfence等指令的开销小。

查看/src/hotspot/share/interpreter/bytecodeinterpreter.cpp 找到执行 getfield 和 getstatic 字节码执行的位置，找到对is_volatile的判断。  

可以看到，在访问对象字段的时候，会先判断它是不是 volatile 的，如果是的话，并且当前 CPU 平台支持多核核 atomic 操作的话（现代的绝大多数的 CPU 都支持），那就是调用 OrderAccess::fence()。

这个函数的实现是具体平台相关的，所以我们就简单看下 linux 平台的 x86 架构的实现：/src/hotspt/os_cpu/linux_x86/orderAccess_linux_x86.hpp

结论是调用的是lock;addl 0指令，也是x86的volatile调用的底层指令是lock。用addl 0操作，并不会改变这个值。但是由于我执行了更改，lock就会把值刷回内存，并废弃其他线程的值。

如果在arm上，/src/hotspt/os_cpu/linux_arm/orderAccess_linux_arm.hpp:调用的是dms_sy指令。


2.也有一种说法是，底层是sfence、lfence内存屏障。volatile内存区的读写都加屏障。waiting check
```shell script
//写操作前后添加指令如下
StoreStoreBarrier
volatile 写操作
StoreLoadBarrier

//读操作前后提那家指令如下
LoadLoadBarrier
volatile 读操作
LoadStoreBarrier
```

如果硬件架构本身已经保证了内存可见性（如单核处理器、一致性足够的内存模型等），那么volatile就是一个空标记，不会插入相关语义的内存屏障。

如果硬件架构本身不进行处理器重排序、有更强的重排序语义（能够分析多核间的数据依赖）、或在单核处理器上重排序，那么volatile就是一个空标记，不会插入相关语义的内存屏障。

如果不保证，仍以x86架构为例，JVM对volatile变量的处理如下：

* 在写volatile变量v之后，插入一个sfence。这样，sfence之前的所有store（包括写v）不会被重排序到sfence之后，sfence之后的所有store不会被重排序到sfence之前，禁用跨sfence的store重排序；且sfence之前修改的值都会被写回缓存，并标记其他CPU中的缓存失效。

* 在读volatile变量v之前，插入一个lfence。这样，lfence之后的load（包括读v）不会被重排序到lfence之前，lfence之前的load不会被重排序到lfence之后，禁用跨lfence的load重排序；且lfence之后，会首先刷新无效缓存，从而得到最新的修改值，与sfence配合保证内存可见性。

在另外一些平台上，JVM使用mfence代替sfence与lfence，实现更强的语义。二者结合，共同实现了Happens-Before关系中的volatile变量规则。

以上sfence、lfence部分出自 https://monkeysayhi.github.io/2017/12/28/%E4%B8%80%E6%96%87%E8%A7%A3%E5%86%B3%E5%86%85%E5%AD%98%E5%B1%8F%E9%9A%9C/


## 2.不适合场景

* a++场景
volatile不保证操作的原子性


## 3.常用x86指令集
mfence指令：上文提到过，能实现全能型屏障，具备lfence和sfence的能力。

cpuid指令：cpuid操作码是一个面向x86架构的处理器补充指令，它的名称派生自CPU识别，作用是允许软件发现处理器的详细信息。

lock指令前缀：总线锁。lock前缀只能加在一些特殊的指令前面。对应总线锁 ，当一个处理器在总线上输出LOCK # 信号时，其他处理器的请求将被阻塞住，那么该处理器可以独占共享内存。
Lock 前缀的指令在多核处理器下会引发两件事情：将当前处理器缓存行的数据写回系统内存。这个写回内存的操作会使在其他 CPU 里缓存了该内存地址的数据无效，该功能通过缓存一致性协议实现。

xadd 指令： fetch and add。fetch和自旋都在CPU层级实现。

cmpxchg 指令： compare and swap。CAS是比较并返回值，自旋CAS需要自己在代码中写while(true)


## happen-before原则与jsr133规范

参考文档: 
http://www.cs.umd.edu/~pugh/java/memoryModel/jsr133.pdf
