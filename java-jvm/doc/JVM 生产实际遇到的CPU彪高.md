
查询期间一般都会要回答CPU升高是因还是果这个问题。
CPU 负载高那可能需要用火焰图看下热点、如果是慢查询增多那可能需要看下 DB 情况、如果是网络流量突然升高要看网络IO、
如果是线程 Block 引起那可能需要看下锁竞争的情况，最后如果各个表象证明都没有问题，那可能 GC 确实存在问题，可以继续分析 GC 问题了。

```shell 
top
top -Hp Pid
printf '%x\n' Pid
jstack Pid | grep Pid16
```

## Docker进程的周期彪高
每分钟触发一次的彪高，最开始的是推测因为ns GC的原因，但是实际触发彪高的主机的NS GC并没有很频繁，那就是靠日志查不出问题。  
1.使用perf record -g；perf report记录CPU高的线程。

参考文档:
https://www.cnblogs.com/fadewalk/p/11184016.html

2.需要使用火焰图看下热点。  
