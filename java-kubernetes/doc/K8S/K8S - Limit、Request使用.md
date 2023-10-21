
一般我们认为Limit、Request中，Request是给调度器看的，仅在调度阶段起左右，Limit是最后的实际上限，会写进主机的cgroups文件中，
以及这两者的比值会被换算成Qos，在容器驱逐等阶段起作用。  

一般来说内存如果满了，OS会进行OOM Kill，kill掉进程。但是CPU如果满了，OS并不会kill进程，只是此时进程分到的时间片全部在计算，响应事件可能会很长。  

但是其少数的情况会触发Out Of CPU这个场景。我们下面讨论CPU Limit带来的另一种情况，即延时上升。

### CPU限流
kubernetes使用CFS()限制负载的CPU使用率。CFS较为复杂，但是 k8s 的文档中给了一个简明的解释，要点如下：
* CPU 使用量的计量周期为 100ms；
* CPU limit 决定每计量周期（100ms）内容器可以使用的 CPU 时间的上限；
* 本周期内若容器的 CPU 时间用量达到上限，CPU 限流开始，容器只能在下个周期继续执行；
* 1 CPU = 100ms CPU 时间每计量周期，以此类推，0.2 CPU = 20ms CPU 时间每计量周期，2.5 CPU = 250ms CPU 时间每计量周期；如果程序用了多个核，CPU 时间会累加统计。

这里以一个API服务举例，当其响应请求时需要2个线程完成工作，分别耗时60ms，80ms。
* 当无CPU Limit时：
则很明显如果两者同时在一个时间周期开始进行工作，则80ms后完成响应。
如果A线程在时间周期开始时工作，B线程在时间周期开始后20ms进行工作，则要100ms后完成响应。

* 当CPU Limit 1C时：
如果A线程在时间周期开始时工作，B线程在时间周期开始后20ms进行工作，则B只能在这个时间周期工作40ms，因为整个进程只有100ms的时间。于是还有40ms会在下个时间周期工作。需要140ms完成响应。即B受到一次限流。

* 当CPU Limit为0.6C时，
如果A线程在时间周期开始时工作，B线程在时间周期开始后20ms进行工作，则A会有1次限流，B有2次限流，220ms后完成响应

并且哪怕此时CPU没有其他工作要做，限流一样会执行。
当然因为一般的API服务是IO密集型工作，占用CPU时间片没有这么大，不会触发多次限流。但是对于计算密集型工作如模型学习等，该限流机制就很麻烦了。此时应尽量提高Limit。

Grafana的CPU Throttling指标就是展示这个的，一般应用限流大概在10%。

并且Grafana因为Pormetheus采集频率的问题，CPU的使用率可能因为信号混叠而无法检查到尖刺，如果统计周期和CFS一样是100ms，或者是1s一次，就能看到尖刺了。

另外Linux内核版本低于4.18时还有个bug会造成不必要的限流。https://github.com/kubernetes/kubernetes/issues/67577#issuecomment-466609030



参考文档：  
https://mp.weixin.qq.com/s/QYJycJCaxB42xdEo3qHHHA
https://kubernetes.io/blog/2018/07/24/feature-highlight-cpu-manager/