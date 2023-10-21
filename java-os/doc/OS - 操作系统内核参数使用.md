
sysctl -a
sysctl -p

## 网络部分

net.ipv4.tcp_tw_reuse   如果开启该选项的话，客户端（连接发起方） 在调用 connect() 函数时，内核会随机找一个 TIME_WAIT 状态超过 1 秒的连接给新的连接复用。
开启tcp_tw_reuse 参数可以快速复用处于 TIME_WAIT 状态的 TCP 连接时，相当于缩短了 TIME_WAIT 状态的持续时间。
在 Linux 操作系统下，TIME_WAIT 状态的持续时间是 60 秒，这意味着这 60 秒内，虽然客户端已经进行了四次挥手的最后一个ack，但客户端一直会占用着这个端口。

net.ipv4.tcp_tw_recycle 如果开启该选项的话，允许处于 TIME_WAIT 状态的连接被快速回

net.ipv4.tcp_timestamps
要使得上面这两个参数生效，有一个前提条件，就是要打开 TCP 时间戳，即 net.ipv4.tcp_timestamps=1（默认即为 1）。开启了 tcp_timestamps 参数，TCP 头部就会使用时间戳选项，它有两个好处，一个是便于精确计算 RTT ，另一个是能防止序列号回绕（PAWS）。

net.ipv4.neigh.default.gc_thresh1  默认128  建议扩大4到10倍
net.ipv4.neigh.default.gc_thresh2  默认512
net.ipv4.neigh.default.gc_thresh3  默认1024
arp overflow相关问题，当使用一些基于二层的网络方案，如HostNetWork、Macvln、VPC CNI时会遇到，并且由于容器会共同占用主机的这个值，当多个容器互相访问时，很少的节点就可以触发。另一种是prometheus扫描所有node-exporter也会触发。
都是arp表满了，导致无法发送包。Pod无法访问Node或新Pod。


参考文档:
https://zhuanlan.zhihu.com/p/450296852

## 文件部分
主要是文件句柄、文件打开数、线程等部分。

一个进程可以持有的最大文件打开数，涉及并发使用，默认1024，也即单个主机只能支持1024个Socket连接。一般调整为65535或100000.
修改/etc/security/limits.conf
soft nofile 100000
hard nofile 100000





