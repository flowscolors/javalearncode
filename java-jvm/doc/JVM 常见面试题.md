
Q:双亲委派机制的作用？  
A:保证Object类从父加载器的路径加载，于是JDK中所需的类是直接加载的。并且classloader在初始化一个类的时候，会对当前类加锁，加锁后，再执行类的静态初始化块，所以如果有循环依赖可能会导致死锁。
   
Q:Tomcat的classloader结构  
A:Java默认是的三次架构，启动类加载器、拓展类加载器、应用程序类加载器。Tomcat的自定义类加载器又分为很多加载器，比如共享类加载器、Catalina加载器。
应用程序类加载器，主要加载classpath路径下的类，在tomcat 的启动脚本里，最终会设置为 bin 目录下的bootstrap.jar 和tomcat-juli.jar

Q:如何自己实现一个classloader打破双亲委派  
A:需要自定义一个classloader，以及自定义一个customLauncher,classloader主要还是使用父类的super()方法，只是传入参数不同，传入自定义的包路径。
还需要修改启动类，在启动类中执行customLauncher.launch()。

Q:Tomcat热部署，热加载,怎么做到的？  
A:所有的热部署、热加载都是基于Tomcat后台线程周期性检测类文件或web应用的变化，重新加载web容器。

Q:如何加载两个不同版本的jar包，同时在一个应用中使用?  
A:重写springboot启动类 org.springframework.boot.loader.JarLauncher，这个是在BOOT-INF/MANIFEST中指定了的。因为默认BOOT-INF/lib下，是由同一个类加载器去加载的，而我们的V1和V2的jar包，全部混在这个目录下。
我们要想同时加载V1和V2的jar包，必须用两个类加载器来做隔离。在原流程中lib目录下的全部包的集合，转成url集合，筛选出中间件的包，复制到单独的集合中，移除原集合中的低版本。创建一个自定义的classloader，主要是方便我们存放中间件相关jar包集合
但是使用过程就无法两边使用一个类名，肯定有一个类需要通过 Class<?> middleWareImplClass 、middleWareImplClass.newInstance();的方法获取调用。
参考文档：https://www.cnblogs.com/grey-wolf/p/13253014.html

---
Q:cms收集器过程  
A:初始标记（STW，仅标记GC Roots，新生代存活对象引用） -> 并发标记（把整个老年代使用三色标记进行计算） -> 重标记 (STW，保证并发标记过程中的也被标记) -> 并发清理 （多线程并发清理）  
初始标记也即Full GC默认在老年代92%触发，所以并发标记的GC回收器都可能会导致GC退化，并发标记、并发回收虽然不会STW，但是会GC线程过多可能会影响业务线程。虽然是并发清理，但是默认开启了标记整理，

Q:g1收集器过程  
A:初始标记（STW，仅标记GC Roots，新生代存活对象引用） -> 并发标记（把整个老年代使用三色标记进行计算） -> 最终标记 (STW，保证并发标记过程中的也被标记) -> 筛选回收 （STW，根据region和价值进行排序，并根据用户期望时间进行垃圾回收，由于这里的操作涉及存活对象的移动，必须暂停用户线程）

Q:g1收集器原理，怎么实现可预测停顿的?  
A:开始了基于region垃圾回收器时代，把堆划分为多个大小相等的Region，每个Region的大小默认情况下是堆内存大小除以2048。

Q:G1 region的大小？  
A:region默认情况下是堆内存大小/2048.所以jmap命令可以看到region大小。当然还有一种 Humongous region，超大内存。

Q:垃圾收集算法，各有什么优缺点  
A:对于JDK8的老年代来说有三种:ParalOld、CMS、G1.而新生代由于采用标记复制，比如ParNew，全程都是STW，所以也并没有调优的必要。
Parallel Old JDK8，server端默认GC回收器，基于标记-整理。优点是可以配合ParNeW 缺点是，该GC回收器并不会按照1：2、2:8来划分新生代老年代，从而导致每次动态扩缩内存空间。
CMS  经典基于标记-清楚的垃圾回收器，JDK14中被移除。优点是 产生时间早 缺点是，并发-整理对大内存支持并不好，可能产生并发标记失败失败退化到单线程标记，
G1   基于region、标记-整理的垃圾回收器.优点，是支持了更大的堆空间。缺点是，更加复杂导致GC内存消耗高10%到20%，可能有并发标记失败、混合回收失败。
ZGC  基于region、标记-整理、JDK11引入，放弃分代，而使用读屏障、染色指针、内存多种映射等技术

Q:gc roots有哪些，什么情况下会发生full gc  
A:gc roots有栈空间的引用对象、静态对象、方法区中的常量引用、本地方法栈JNI（Native方法）引用的对象、Java虚拟机内部的引用、系统类加载器、同步锁持有的对象··· ··· 
Full GC一般会在老年代快满的情况触发，CMS默认92%触发，可参数调节。G1则是新生代+老年代占堆内存45%时触发mix gc。

Q:对象一定分配在堆上么，JIT，分层编译，逃逸分析  
A:不一定在堆上，还可以在栈上（内部类）或者在本地内存（直接内存操作）。JIT做为一种把字节码优化成机器码的操作，被广泛使用，但是在JDK15中被移除。
分层编译指JIT编译器的C1、C2、混合编译，一般默认开启的混合编译。
逃逸分析，编译器的最重要的优化手段。分析对象的动态作用域，比如方法中对象因为方法被外部调用而传递到其他方法、也可以直接可以被外部线程访问。如果能证明一个对象不会逃逸到方法或线程之外，
或者逃逸程度较低，那就可以使用不同的优化手段，比如栈上分配、标量替换、同步消除等来进行优化。

Q:new Object[100]对象大小，它的一个对象引用大小，对象头结构  
A:对象大小由于是数组的引用的大小，默认开启指针压缩，一行为32 bits，即4 bytes。单Object为12 bytes。Object[100]为416 bytes。
对象头结构：Mark Word + 存储指向方法区对象类型的指针，如果是数组，还会记录数组长度  + 填充字段。

Q:jvm内存结构，堆结构，栈结构，a+b操作数栈过程，方法返回地址什么时候回收，程序计数器什么时候为空  
A:JVM内存结构：线程共有的Java虚拟机堆、方法区；线程私有的Java虚拟机栈、本地方法栈、程序计数器。堆结构一般是新生代，老年代 1：2，新生代1：1：8的 Eden :Surivion。
栈由栈帧构成，栈帧里面存储着局部变量表（Slots）、操作数栈、动态连接、方法出口等信息。
a+b操作数栈，a、b从局部变量表中获取值，获取+，进行计算，结果返回操作数栈，出栈，返回上一个方法出口。
方法返回地址，不会被回收，线程死亡时才会被回收，局部变量表（Slots）可以重复利用。
程序计数器，在没有方法调用，或者结束时为空。

Q:GC Root、ModUnionTable   
A:gc roots有栈空间上的引用对象、静态对象、方法区的常量引用、本地方法栈JNI(Native方法)引用的对象、Java虚拟机内部的应用、系统类加载器、同步锁持有的对象··· ···

Q:什么是TLAB?  
A:线程本地分配缓冲区（Thread Local Allocation Buffer），Java堆中还可以划分成多个线程私有的分配缓冲区，以提升对象分配的效率。

Q:finalize()方法的使用？  
A:真正要宣告一个对象死亡，至少要经过2次标记过程，第一次是可达性分析有没有在GC Root的引用链上。二是在真正GC前执行finalize()方法，如果能再建立引用，则会逃过GC。该功能在JDK16中被废弃。

Q:三色标记算法？  
A:并发的可达性分析，··· ···

---
Q:jvm了解哪些参数，用过哪些指令  
A:主要三类参数。
内存结构、编译器相关:-Xmx、-Xms、-Xss、-XX:NewRatio、-XX:SurvivorRatio、-XX:MaxMetaspaceSize、-XX:MetaspaceSize、-XX:MaxDirectMemorySize、-XX:CICompileCount。
性能辅助查询：-verbose:gc、-XX:+PrintGCDetails、-XX:+PrintGCTimeStamps、-Xloggc:/logs、-XX:NumberOfGCLogFiles、-XX:GCLogFileSize、-XX:OmitStackTraceInFastThrow、-XX:+HeapDumpOnOutOfMemoryError、-XX:+HeapDumpPath=/logs
GC垃圾回收器与特定GC回收器相关参数配置:-XX:ParallelGCThread、-XX:+UseConcMarkSweepGC、-XX:CMSInitiatingOccupancyFraction、-XX:+UseCMSInitiatingOccupancyFractionOnly、-XX:-XX:+CMSScavengeBeforeRemark、-XX:+UseG1GC、-XX: MaxGCPauseMillis、-XX:G1HeapRegionSize、-XX:G1MixedGCCountTarget
其他参数: -Dfile.encoding=UTF-8、-Dspring.config.location=./application.yml、-javaagent:*.jar、远程调试的参数。

Q:内存溢出，内存泄漏遇到过吗？什么场景产生的？怎么解决的？  
A:内存溢出一般指临时大对象生成导致的单次OOM，一般读取大文件，大批量的SQL查询会导致该结果。内存溢出则指缓慢的对象占用没释放，一般是某些容器类操作时会触发，比如业务的hashmap没有remove，threadlocal的value值也是一个经典场景。

Q:锁升级过程，轻量锁可以变成偏向锁么？偏向锁可以变成无锁么？自旋锁，对象头结构，锁状态变化过程  
A:不会变成偏向锁，只会升级成重量级锁。偏向锁不会变成无锁，准确说无锁这个状态在JVM中就很少存在，因为默认启动4s之后生成的对象直接就带偏向锁。
自旋锁就是轻量级锁的表现，对象头结构（），锁状态变化：无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁

Q:如何debug JDK？  
A:单纯修改代码进行编译，直接用容器就可以。如果需要有工具Debug，需要上NetBean。 https://www.cnblogs.com/grey-wolf/p/10971741.html

参考文档：  
https://www.jianshu.com/p/aa6d1c32d104