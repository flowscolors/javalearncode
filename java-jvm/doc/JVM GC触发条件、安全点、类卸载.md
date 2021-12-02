

JDK8内存结构。

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/JDK8-内存结构.jpg)

## GC Roots
在Java中，可作为 GC Roots 对象的一般包括如下几种：

* Java虚拟机栈中的引用的对象，如方法参数、局部变量、临时变量等 
* 方法区中的类静态属性引用的对象 
* 方法区中的常量引用的对象，如字符串常量池的引用 
* 本地方法栈中JNI的引用的对象
* Java虚拟机内部的引用，如基本数据类型的 Class 对象，系统类加载器等
```
public class GCDemo {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        loadAccount();
    }

    public static void loadAccount() {
        Account account1 = new Account("T001");
        Account account2 = new Account("T002");

        List<Account> accountList = new ArrayList<>();
        accountList.add(account1);
        accountList.add(account2);

        MAPPER.writeValueAsString(accountList);
    }
}
```
上例中，类静态变量 MAPPER，loadAccount 方法的局部变量 account1、account2、accountList 都可以作为 GC Roots（ArrayList 内部是用 Object[] elementData 数组来存放元素的）。

在调用 loadAccount 方法时，堆中的对象都是可达的，因为有 GC Roots 直接或间接引用到这些对象，此时若发生垃圾回收，这些对象是不可被回收的。loadAccount 执行完后，弹出栈帧，方法内的局部变量都被回收了，虽然堆中 ArrayList 对象还指向 elementData 数组，而 elementData 指向 Account 对象，但没有任何 GC Roots 的引用链能达到这些对象，因此这些对象将变为垃圾对象，被垃圾回收器回收掉。

在Controller、Service层中我们经常会写很多new ArrayList，用来存SQL结果或者返回值，这种都是执行完就会被GC的。

## GC的相关原因
查看相关代码，JVM报GC的原因大概有以下部分：
```
#include "precompiled.hpp"
#include "gc/shared/gcCause.hpp"

const char* GCCause::to_string(GCCause::Cause cause) {
  switch (cause) {
    case _java_lang_system_gc:
      return "System.gc()";

    case _full_gc_alot:
      return "FullGCAlot";

    case _scavenge_alot:
      return "ScavengeAlot";

    case _allocation_profiler:
      return "Allocation Profiler";

    case _jvmti_force_gc:
      return "JvmtiEnv ForceGarbageCollection";

    case _gc_locker:
      return "GCLocker Initiated GC";

    case _heap_inspection:
      return "Heap Inspection Initiated GC";

    case _heap_dump:
      return "Heap Dump Initiated GC";

    case _wb_young_gc:
      return "WhiteBox Initiated Young GC";

    case _wb_conc_mark:
      return "WhiteBox Initiated Concurrent Mark";

    case _wb_full_gc:
      return "WhiteBox Initiated Full GC";

    case _update_allocation_context_stats_inc:
    case _update_allocation_context_stats_full:
      return "Update Allocation Context Stats";

    case _no_gc:
      return "No GC";

    case _allocation_failure:
      return "Allocation Failure";

    case _tenured_generation_full:
      return "Tenured Generation Full";

    case _metadata_GC_threshold:
      return "Metadata GC Threshold";

    case _metadata_GC_clear_soft_refs:
      return "Metadata GC Clear Soft References";

    case _cms_generation_full:
      return "CMS Generation Full";

    case _cms_initial_mark:
      return "CMS Initial Mark";

    case _cms_final_remark:
      return "CMS Final Remark";

    case _cms_concurrent_mark:
      return "CMS Concurrent Mark";

    case _old_generation_expanded_on_last_scavenge:
      return "Old Generation Expanded On Last Scavenge";

    case _old_generation_too_full_to_scavenge:
      return "Old Generation Too Full To Scavenge";

    case _adaptive_size_policy:
      return "Ergonomics";

    case _g1_inc_collection_pause:
      return "G1 Evacuation Pause";

    case _g1_humongous_allocation:
      return "G1 Humongous Allocation";

    case _dcmd_gc_run:
      return "Diagnostic Command";

    case _last_gc_cause:
      return "ILLEGAL VALUE - last gc cause - ILLEGAL VALUE";

    default:
      return "unknown GCCause";
  }
  ShouldNotReachHere();
}
```

这里解释几种常见的GC原因：  


[Full GC (Ergonomics)] 空间分配担保机制。一般Ergonomics是因为晋升到老年代的平均大小大于老年代的剩余大小，则提前进行一次Full GC。而对象如果超过Eden区一半大小，也会直接去老年代。
所以出现这种情况一般是出现了大内存对象，比如查询没带Limit。也可能是因为同时创建很多同类对象，因为JVM 的动态年龄机制是如果在 Survivor 中相同年龄所有对象大小的总和大于 Survivor 空间的一半, 则年龄大于或等于该年龄的对象可以直接进入老年代，同时创建很多则2次就可以去老年代了。  

  
[Metadata GC Threshold] 方法区回收。方法区垃圾回收的“性价比”通常是比较低的，方法区的垃圾回收主要回收两部分内容：废弃的常量和不再使用的类型。废弃的常量即常量池中废弃的字面量，字段、方法的符号引用等。废弃的类，需要满足该类所有实例被回收、ClassLoader被回收（这点其实很难达成，除非设计了一种替换类加载器的机制）、java.lang.Class对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。  
在大量使用反射、动态代理、CGLib等字节码框架，动态生成JSP以及OSGi这类频繁自定义类加载器的场景中，通常都需要Java虚拟机具备类型卸载的能力，以保证不会对方法区造成过大的内存压力。

[Concurrent Mode Failure] 并发运行失败。 G1 GC、CMS GC 运行期间，Old 区预留的空间不足以分配给新的对象，此时收集器会发生退化，严重影响 GC 性能。


参考文档：  
https://tech.meituan.com/2017/12/29/jvm-optimize.html  
https://tech.meituan.com/2020/11/12/java-9-cms-gc.html  


## STW
1）STW：
可达性分析算法从 GC Roots 集合找引用链时，需要枚举根节点，然后从根节点标记存活的对象，根节点枚举以及整理内存碎片时，都会发生 Stop The World，此时 jvm 会直接暂停应用程序的所有用户线程，然后进行垃圾回收。因为垃圾回收时如果还在继续创建对象或更新对象引用，就会导致这些对象可能无法跟踪和回收、跟节点不断变化等比较复杂的问题，因此垃圾回收过程必须暂停所有用户线程，进入 STW 状态。垃圾回收完成后，jvm 会恢复应用程序的所有用户线程。  

2）安全点（Safe Point）：

用户程序执行时并非在代码指令流的任意位置都能够停顿下来开始垃圾收集，而是强制要求必须执行到达安全点后才能够暂停。安全点可以理解成是在代码执行过程中的一些特殊位置，当线程执行到这些位置的时候，说明虚拟机当前的状态是安全的，如果有需要，可以在这个位置暂停。

安全点位置的选取基本上是以“是否具有让程序长时间执行的特征”为标准进行选定的，“长时间执行”的最明显特征就是指令序列的复用，例如方法调用、循环跳转、异常跳转等都属于指令序列复用，所以只有具有这些功能的指令才会产生安全点。

jvm 采用主动式中断的方式，在垃圾回收发生时让所有线程都跑到最近的安全点。主动式中断的思想是当垃圾回收需要中断线程的时候，不直接对线程操作，仅仅简单地设置一个标志位，各个线程执行过程时会不停地主动去轮询这个标志，一旦发现中断标志为真时就自己在最近的安全点上主动中断挂起。

3）安全区域（Safe Region）：

安全点机制保证了程序执行时，在不太长的时间内就会遇到可进入垃圾回收过程的安全点。但是，程序“不执行”的时候，线程就无法响应虚拟机的中断请求，如用户线程处于Sleep状态或者Blocked状态，这个时候就没法再走到安全的地方去中断挂起自己。这就需要安全区域来解决了。

安全区域是指能够确保在某一段代码片段之中，引用关系不会发生变化，因此，在这个区域中任意地方开始垃圾回收都是安全的。当用户线程执行到安全区域里面的代码时，首先会标识自己已经进入了安全区域，那样当这段时间里虚拟机要发起垃圾回收时就不必去管这些已声明自己在安全区域内的线程了。当线程要离开安全区域时，它要检查虚拟机是否已经完成了需要暂停用户线程的阶段，如果完成了，那线程就继续执行；否则它就必须一直等待，直到收到可以离开安全区域的信号为止。


## TLAB
Thread Local Allocation Buffer 的简写，基于 CAS 的独享线程（Mutator Threads）可以优先将对象分配在 Eden 中的一块内存，因为是 Java 线程独享的内存区没有锁竞争，所以分配速度更快，每个 TLAB 都是一个线程独享的。

## Card Table
 中文翻译为卡表，主要是用来标记卡页的状态，每个卡表项对应一个卡页。当卡页中一个对象引用有写操作时，写屏障将会标记对象所在的卡表状态改为 dirty，卡表的本质是用来解决跨代引用的问题。具体怎么解决的可以参考 StackOverflow 上的这个问题 ![how-actually-card-table-and-writer-barrier-works](https://stackoverflow.com/questions/19154607/how-actually-card-table-and-writer-barrier-works)，或者研读一下 cardTableRS.app 中的源码。
 
 