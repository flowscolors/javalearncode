## 1.创建线程的4种/一种方法
一般我们回答创建线程的方法，会答有4种:
1.直接new Thread或者继承Thread类，重写run()方法，再new。  
2.实现runnable接口，然后重写run()方法，再把实现了run()方法的实例传到Thread中即可。  
3.线程池创建线程。实际线程池也是依赖DefaultThreadFactory去new Thread。  
4.实现callable接口，重写call()方法，可以被放到线程或线程池中执行。  

仔细考虑一下，runnable、callable、futuretask其实本身并不是线程，而是定义了需要执行的任务。最后执行是线程或线程池。  
那么以朴素的思想，我们认为只有构造一个Thread类，才是创建线程的唯一方法。
```
@Override

public void run() {

    if (target != null) {

        target.run();

    }

}
```
启动线程会调用start()方法，而start()最后是要调run()的，只是传Runable会执行runnable的run，new Thread时直接重写。  

PS:但是一般认为Runable实现比Thread继承好。
1.因为我的实现类并不一定需要Thread的一些属性，只实现一个run()方法，实现了与Thread类的解耦。 
2.Runable可以选择传递给线程或者线程池，但是new Thread就只能是线程了。对于大量重复操作，需要线程池。  
3.Java不能双继承，继承Thread，以后就不能继承其他的了。 

waiting code  
已实现 Thread.ThreadInitDemo

## 2.如何停止线程 interrupted
一般我们不会手动停止一个线程，而是运行一个线程运行到结束，自然停止后由OS进行回收。但是当某些特殊情况或者意外发生时就需要我们程序控制停止线程，比如： 程序异常报错无法进行、用户操作异常。  

事实上，Java 希望程序间能够相互通知、相互协作地管理线程，因为如果不了解对方正在做的工作，贸然强制停止线程就可能会造成一些安全的问题，为了避免造成问题就需要给对方一定的时间来整理收尾工作。比如：线程正在写入一个文件，这时收到终止信号，它就需要根据自身业务判断，是选择立即停止，还是将整个文件写入成功后停止，而如果选择立即停止就可能造成数据不完整，不管是中断命令发起者，还是接收者都不希望数据出现问题。  

所以Java设计了interrupt机制。  
我们一旦调用某个线程的 interrupt() 之后，这个线程的中断标记位就会被设置成 true。每个线程都有这样的标记位，当线程执行时，应该定期检查这个标记位，如果标记位被设置成 true，就说明有程序想终止该线程。并且该标志位在sleep时也会考虑。 

waiting code

## 3.线程的6种状态
就像生物从出生到长大、最终死亡的过程一样，线程也有自己的生命周期，在 Java 中线程的生命周期中一共有 6 种状态。

New（新创建）
Runnable（可运行）
Blocked（被阻塞）
Waiting（等待）
Timed Waiting（计时等待）
Terminated（被终止）
如果想要确定线程当前的状态，可以通过 getState() 方法，并且线程在任何时刻只可能处于 1 种状态。

waiting code

需要注意的是：
线程的状态是需要按照箭头方向来走的，比如线程从 New 状态是不可以直接进入 Blocked 状态的，它需要先经历 Runnable 状态。  
线程生命周期不可逆：一旦进入 Runnable 状态就不能回到 New 状态；一旦被终止就不可能再有任何状态的变化。所以一个线程只能有一次 New 和 Terminated 状态，只有处于中间状态才可以相互转换。 


## 4.上下文切换
在这个运行过程中，线程由 RUNNABLE 转为非 RUNNABLE 的过程就是线程上下文切换。 我们可以用vmstat pidstat查看上下文切换次数。

参考文档:
https://www.geekschool.org/2020/07/18/8868.html
http://blog.objectspace.cn/2019/10/13/%E6%9C%80%E9%80%82%E5%90%88%E5%88%9D%E5%AD%A6%E8%80%85%E4%BA%86%E8%A7%A3%E7%9A%84Java%E5%A4%9A%E7%BA%BF%E7%A8%8B%E4%B8%8E%E5%B9%B6%E5%8F%91%E5%9F%BA%E7%A1%80/

