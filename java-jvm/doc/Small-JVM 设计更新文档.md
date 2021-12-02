

## 框架搭建与解析class文件
### 1.搭建命令行框架
使用第三方包jcommander，新增Main.java、Cmd.java
在main函数中定义version cmd的返回值。如果是上述则直接return，否则执行startJVM。

### 2.搜索class文件
本章主要介绍如何实现让虚拟机加载class文件。  
分成三部分Entry、ClassPath、startJVM
* Entry 作为一个接口
* ClassPath的对象 用来完成对应路径下class文件的字节数组加载
* startJVM中添加获取classPath、className，调用ClassPath加载，以及把class文件进行16进制输出展示代码
* Cmd类中添加jre字段

启动参数：  
//program arguments：-Xjre "D:\codeSource\jdk1.8.0_102\jre" D:\codeProject\javaProject\javalearncode\java-jvm\samll-jvm\target\test-classes\jvm\test\HelloWorld

### 3.解析class文件
本章主要介绍如何实现让虚拟机解析class文件。  
上一步已经能从文件中读取class文件，现在是要把16进制的class文件解析出对应的：class文件、常量池、属性表。
> 作为类（或者接口）信息的载体，每个class文件都完整地定义了一个类。Java虚拟机规范对class文件格式进行了严格的规定。但是另外一方面，对于从哪里加载class文件，给了足够多的自由。
> Java虚拟机实现可以从文件系统读取和从JAR（或ZIP）压缩包中提取clss文件。除此之外，也可以通过网络下载、从数据库加载，甚至是在运行中直接生成class文件。Java虚拟机规范中所指的class文件，并非特指位于磁盘中的.class文件，而是泛指任何格式符号规范的class数据。

新增classfile部分主要分为：  
* attributes        用来描述属性表中的各种属性
    ** 很多种属性，比如BootstrapMethods、Code、ConstantValue等，每种属性都要从data中读取自己部分的相应byte[],并解释成对应含义存到自己的字段中
* constantpool      用来描述常量池中的各种常量
    ** ConstantPool 常量池，ConstantInfo[] constantInfos; 因为data文件中有常量池容量计数值，只要拿到这个，就知道了常量池大小，就可以解析了。并且每个常量前面有tag标志位记录类型。  
    ** ConstantInfo 常量，在接口中定义各种静态类型对于的int值，从基本类型到对象、反射。 包含静态变量的className。  
       每一种常量都对应着jvm中定义的常量，内含常量池和自己在常量池的偏移量，每种常量都要从data中读取自己部分的相应byte[],并解释成对应含义存到自己的字段中
* ClassFile.java    用来描述class文件成员变量，也即魔数、版本号、常量池、访问标志、属性表集合、类索引与字段索引、字段表集合、方法表集合
* ClassReader.java  用来直接输入上一步的字节数组，封装一些常用方法提供给classFile，提取data的前1、2、4、8位，并返回int、long、double、float类型。
* MemberInfo.java   用来描述字段表集合、方法表集合中的字段、方法
* 以及修改startJVM方法，让其在上一步的加载步骤后，新增一步解析程序。  

补充：  
class文件是一种字节码，所以全平台统一支持。class文件的结构是以8字节为基础单位的二进制流。  
对于无符号数，也即基本数据类型，有u1、u2、u4、u8，代表1、2、4、8字节存储。用来描述数字、索引引用、数量值或按照UTF-8编码成字符串。  
表，多个无符号数或其他表作为数据项构成的复合数据结构，以_info结尾。用来描述有层次关系的复合结构数据。

ClassFile结构体 u1[1字节=8比特位]、u2[2字节=2×8比特位]、u4[4字节=4×8比特位]
```
u4 magic;
u2 minor_version;
u2 major_version;
u2 constant_pool_count;
cp_info constant_pool[constant_pool_count-1];
u2 access_flags;
u2 this_class;
u2 super_class;
u2 interfaces_count;
u2 interfaces[interfaces_count];
u2 fields_count;
field_info fields[fields_count];
u2 methods_count;
method_info methods[methods_count];
u2 attributes_count;
attribute_info attributes[attributes_count];
```  

## 解析方法并依靠指令集执行计算
###  4.实现运行时数据区   准备区间，是后面Part的基础
本章主要介绍如何实现运行时区域的Java虚拟机、程序计数器，虚拟机栈用来存放栈帧、操作数栈、局部变量表，实现后就可以使用线程进行方法计算了。
  
运行时数据区: Java虚拟机堆、Java虚拟机栈、本地方法栈、程序计数器、方法区。
> 线程私有的运行时数据区(程序计数器、Java虚拟机栈)用于辅助执行Java字节码。每个线程都有自己的pc寄存器（Program Counter）和Java虚拟机栈（JVM Stack）。
> Java虚拟机栈又由栈帧（Stack Frame，后面简称帧）构成，帧中保存方法执行的状态，包括局部变量表（Local Variable）和操作数栈（Operand Stack）等。
> 在任一时刻，某一线程肯定是在执行某个方法。这个方法叫作该线程的当前方法；执行该方法的帧叫作线程的当前帧；声明该方法的类叫作当前类。如果当前方法是Java方法，则pc寄存器中存放当前正在执行的Java虚拟机指令的地址，否则，当前方法是本地方法，pc寄存器中的值没有明确定义。  

![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/jvm-运行时数据区.png)

在这一章将把class文件的内容加载到Java虚拟机栈，并打印操作数栈、局部变量表里的内容。

新增rtda部分文件，主要分为：  
* Frame         栈帧
* JvmStack      Java虚拟机栈
* LocalVars     局部变量表
* OperandStack  操作数栈
* Slot          数据槽
* Thread        线程
* 修改main文件startJVM方法，

### 5.指令集和解释器  上
本章主要介绍如何实现让虚拟机处理基本计算。    
> 每一个类或者接口都会被Java编译器编译成一个class文件，类或接口的方法信息就放在class文件的method_info结构中。
> 如果方法不是抽象的，也不是本地方法，方法的Java代码就会被编译器编译成字节码（即使方法是空的，编译器也会生成一条return语句），存在method_info结构的Code属性中。 

新增： 
* Factory类               指令工厂类，newInstruction方法根据输入的不同字节码返回对应的指令对象，具体的指令对象在instructions目录下定义
* Instruction接口         指令接口，所有具体的指令都需要实现该接口。三个方法，取操作数、执行计算、分支（计算栈帧对应程序器的offerset，放到栈帧的nextPC里）
* Interpret指令集解释器      解释器构造函数中会读取字节码，用其中参数设置线程、栈帧，执行loop操作。 loop操作中进行循环，每次读取当前指令的字节码，生成对应对象进行操作，每次计算结果更新到栈帧中。
* 更新上一步骤所需的Thread Frame OperandStack LocalVars类，新增一些字段和方法
* 修改main文件startJVM方法，执行指令集解释器

新增instructions部分文件，主要分为：  
* base            基本操作，定义接口，读取字节等方法。之所以说是base，是因为后面的所有指令是基于base指令进行延申的，这里有最基础的指令方法。
* comparisons     
* constants
* control
* conversions
* extended
* load
* math
* stack
* stores


### 6.类和对象   中 
本章主要介绍如何实现运行时区域的heap区，用来存放类和对象，实现后就虚拟机就可以输出计算结果了。
续接上一part，完善return对应的指令集，方法栈最后被执行完，走到empty。   

新增：  
* ClassLoader  很明显classloader就是读取classPath路径下的文件，并在堆区生成对应的Class对象，存到Map<String, Class>中，Class也是我们自己定义的。  
* methodarea
** Class         类定义 ， 比较关键的字段有instanceSlotCount会用来计算一个对象需要多少数据槽
** ClassMember   类中成员的定义
** Field         字段，继承ClassMember，因为字段也是类中的成员
** Method        方法，继承ClassMember，因为方法也是类中的成员
** Object        对象，有Class字段和Slots字段
** Slots         数据槽组，内含数据槽数组。

* constantpool
** AccessFlags   定义accessflag对应的静态变量
** RunTimeConstantPool 运行时常量池，主要是用一个java.lang.Object[]数组来存运行时常量。其实我们可以从读取class文件时，读取到每个类的常量池，只要把这些都放到一起，就是运行时常量池了。但是需要注意的是此处的依旧是每个类自己的，并非所有的总和。    
** SymRef        系统引用，内含运行时常量池、类名和类，返回运行时常量池对应的class
** ClassRef      类引用，继承SymRef。新增newClassRef方法。
** MemberRef     成员引用，继承ClassRef。新增copyMemberRefInfo方法。
** FieldRef      字段引用，继承MemberRef。新增newFieldRef、resolvedField、resolveFieldRef、lookupField方法
** MethodRef     方法引用，继承MemberRef。新增newMethodRef、ResolvedMethod、resolveMethodRef方法

* 修改slot、LocalVars、OperandStack的object为该部分定义的objetct。
* 修改指令集中所有涉及Object的指令，已经完善上一part遗漏、待完善的指令，新增references、return部分指令。
* 修改Interpret指令集解释器中switch部分。
* Frame中新增method字段，修改Frame构造函数，初始化时传入method，操作数栈和局部变量表大小从method中读取。


### 7.方法调用和返回  下
本章主要介绍如何实现让虚拟机处理方法调用。把寄存器计算出来的值输出给···    
本章节主要用java实现；方法调用指令、返回指令、解析方法符号引用、参数传递等。实现新的指令后我们的虚拟机就可以执行稍微复杂的运算并输出结果。

> 从调用的角度来看，方法可以分为两类：静态方法（类方法）和实例方法。静态方法通过类调用，实例方法通过对象引用调用。
> 静态方法是静态绑定的，调用哪个方法在编译器就已经确定。实例方法则支持动态绑定，最终调用哪个方法要到运行期才知道。

> 从实现的角度来看，方法可以分为三类：没有实现（也就是抽象方法）、用Java语言（或者JVM上其他的语言，如Groovy和Scala等）实现和用本地语言（如C或者C++）实现。
> 静态方法和抽象方法是互斥的。在Java 8之前，接口只能包括抽象方法。为了实现Lambda表达式，Java 8放宽了这一限制，在接口中也可以定义静态方法和默认方法。

> 在Java 7之前，Java虚拟机规范一共提供了4条方法调用指令。其中invokestatic指令用来调用静态方法。invokespecial指令用来调用无须动态绑定的实例方法，包括构造函数、私有方法和通过super关键字调用的超类方法。剩下的情况则属于动态绑定。如果是针对接口类型的引用调用方法，就使用invokeinterface指令，否则使用invokevirtual指令。

* heap.methodarea中新增方法的有关的变量传入、解释执行等文件： 
** MethodDescriptor
** MethodDescriptorParser
** MethodLookup

* 指令集中新增instructions.references部分文件：  
** INVOKE_INTERFACE
** INVOKE_SPECIAL
** INVOKE_STATIC
** INVOKE_VIRTUAL

* LocalVars、OperandStack、Method、MethodRef、InterfaceMethodRef中新增部分调用方法

* Interpret指令解释器中更新方法相关的部分文件。 

bug待修复，使用斐波那契的例子时，无法读取到java/lang/Object.<clinit>() 	寄存器(指令)：0xb1 -> RETURN => 局部变量表：null 操作数栈：null
循环相加例子可以正常使用。问题推测在默认Object与自定义的Object类的使用。
```
jvm/test/HelloWorld.main() 	寄存器(指令)：0x40 -> LSTORE_1 => 局部变量表：[{"num":0},{"num":0},{"num":0}] 操作数栈：[{"num":10},{"num":0},{"num":0}]
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: -2
	at jvm.rtda.OperandStack.popLong(OperandStack.java:51)
	at jvm.instructions.base.InstructionNoOperands._lstore(InstructionNoOperands.java:85)
```


## JVM三方面功能增强
### 8.数组和字符串
本章主要介绍如何实现让虚拟机处理数组和字符串。  
在虚拟机功能增强后，我们可以执行数组类型计算和输出字符串。本章需要新增实现数组指令；newarray、anewarray、arraylength、<t>aload、<t>astore、multianewarray、ldc，同时需要需要开发字符串池方法等。

首先，数组类和普通的类是不同的。普通的类从class文件中加载，但是数组类由Java虚拟机在运行时生成。数组的类名是左括号([)+数组元素的类型描述符；数组的类型描述符就是类名本身。例如，int[]的类名是[I，int[][]的类名是[[I，Object[]的类名是[Ljava/lang/Object;，String[][]的类名是[[java/lang/String;，等等。  
其次，创建数组的方式和创建普通对象的方式不同。普通对象new指令创建，然后由构造函数初始化。基本类型数组由newarray指令创建；引用类型数组由anewarray指令创建；另外还有一个专门的mulitianewarray指令用于创建多维数组。  
最后，很显然，数组和普通对象存在的数据也是不同的。普通对象中存放的是实例变量，通过putfield和getfield指令存取。数组对象中存放的则是数组元素，通过<t>aload和<t>astore系列指令按索引存取。其中<t>可以是a、b、c、d、f、i、l或者s，分别用于存取引用、byte、char、double、float、int、long或者shore类型的数组。另外，还有一个arraylength指令，用于获取数组长度。  

* 指令集中新增instructions.references部分文件： 
** ANEW_ARRAY
** ARRAY_LENGTH
** MULTI_ANEW_ARRAY
** NEW_ARRAY


### 9.本地方法调用
本章主要介绍如何实现让虚拟机调用本地方法。  
Java虚拟机和Java类库一起构成了Java运行时环境。Java类库主要用Java语言编写，一些无法用Java语言实现的方法则使用本地语言编写，这额方法叫作本地方法。 OpenJDK类库中的本地方法是用JNI（Java Native Interface）编写的，但是要让虚拟机支持JNI规范还需要大量工作。


### 10.异常处理
本章主要介绍如何实现虚拟机处理抛出的异常。

在Java语言中，异常可以分为两类：Checked异常和Unchecked异常。Unchecked异常包括java.lang.RuntimeException、java.lang.Error以及它们的子类，提前异常都是Checked异常。所有异常都最终继承自java.lang.Throwable。如果一个方法有可能导致Checked异常抛出，则该方法要么需要捕获该异常并妥善处理，要么必须把该异常列在自己的throws子句中，否则无法通过编译。Unchanged异常没有这个限制。请注意，Java虚拟机规范并没有这个规定，这只是Java语言的语法规则。


Java虚拟机中在方法的异常处理表中定义相关数据。
