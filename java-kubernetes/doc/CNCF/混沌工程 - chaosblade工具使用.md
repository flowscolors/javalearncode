## 1.chaosblade 介绍
一款基于命令行的故障注入工具，之后在Kubernertes环境中有Operator形态，并且提供了前端页面进行操作。
并且命令行提供server端，可以基于接口进行故障注入。

架构:
chaosblade-box调用每台主机上的chaosblade或chasoblade operator进行相关实验。
具体chaosblade以插件的模式支持各个混沌工程实验：
    * chaosblade-exec-jvm 支持Java故障注入
    基于jvm-sandbox实现，通过java agent机制动态加载agent jar。而每个模块的agent jar用来对dubbo servlet mysql的指定类进行故障注入。
    * chaosblade-exec-os
    基于go实现，封装基本的故障场景。支持的组件包括 CPU、内存、网络、磁盘、进程、shell 脚本等。原理是使用linux或者golang语言，以及cgroup管理来进行故障注入。   
     **   以网络延迟为例，实际调用就是linux里的tc命令进行延迟、丢包的模拟  
     ![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/chaosblade-network.png)
     **   以CPU-Burn为例，在以前的版本中是使用stress命令，在新的版本中直接使用go代码模拟
     ![](https://cdn.jsdelivr.net/gh/flowscolors/resources-backup@main/img_bed/chaosblade-burncpu.png)
    * chaosblade-exec-docker  
    基于go实现，封装对docker的故障场景
    * chaosblade-operator  
    基于operator实现，实现CRD来表现对Node Pod Container的故障场景。

参考文档：
https://github.com/chaosblade-io/chaosblade-exec-jvm/wiki/%E6%96%B0%E6%89%8B%E6%8C%87%E5%8D%97

## chaosblade常用命令

./balde create cpu/disk/men/network/docker/k8s/jvm/servlet
./blade destory
./balde status 
./blade server
./blade prepare
./blade revoke

参考文档：
中文手册 https://chaosblade-io.gitbook.io/chaosblade-help-zh-cn/bladec

## 混沌工程平台chaosblade-box
另外目前看来chaosblade还是有很多需要做的工作，比如agent安装容易失败，没有权限认证，前端代码未开源。  
以及实际实验时，为了验证真正交易的成功率，是需要引入测试流量的，无论是测试的人发压还是导入部分脱敏的生产流量，基本要落地是一定要进行定制化的。


