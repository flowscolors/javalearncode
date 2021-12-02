## shutdown与shotdownNow

调用shutdown或者shutdownNow，两者都不会接受新的任务，而且通过调用要停止线程的interrupt方法来中断线程，需要注意有可能线程永远不会被中断。  
不同之处在于shutdownNow会首先将线程池的状态设置为STOP，然后尝试停止所有线程（有可能导致部分任务没有执行完）然后返回未执行任务的列表。
而shutdown则只是将线程池的状态设置为shutdown，然后中断所有没有执行任务的线程，并将剩余的任务执行完。


5种关闭线程池有关方法
5 种在 ThreadPoolExecutor 中涉及关闭线程池的方法，如下所示。
void shutdown()  可以安全地关闭一个线程池，但是要执行完线程池中现在的任务。   
boolean isShutdown()  返回 true 或者 false 来判断线程池是否已经开始了关闭工作，也就是是否执行了 shutdown 或者 shutdownNow 方法。  
boolean isTerminating()  如果正在中止则返回true。
boolean isTerminated()  可以检测线程池是否真正“终结”了，这不仅代表线程池已关闭，同时代表线程池中的所有任务都已经都执行完毕了    
boolean awaitTermination(long timeout, TimeUnit unit) 调用 awaitTermination 方法后当前线程会尝试等待一段指定的时间，如果在等待时间内，线程池已关闭并且内部的任务都执行完毕了，也就是说线程池真正“终结”了，那么方法就返回 true，否则超时返回 fasle。  
List<Runnable> shutdownNow();  5 种方法里功能最强大的，立刻关闭。在执行 shutdownNow 方法之后，首先会给所有线程池中的线程发送 interrupt 中断信号，尝试中断这些任务的执行，然后会将任务队列中正在等待的所有任务转移到一个 List 中并返回，我们可以根据返回的任务 List 来进行一些补救的操作，例如记录在案并在后期重试。


waiting code


## 线程池如何做到线程复用的

Worker内部类的工作流程。


waiting code