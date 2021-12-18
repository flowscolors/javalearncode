

总的来讲，Volcano是基于Kubernetes构建的一个通用批量计算系统；帮忙 Kubernetes 弥补在“计算类任务”方面不足，帮助Kubernetes构建统一的容器平台。

kube-batch 仅是一个调度器，为计算类作业提供了相应的调度算法支持；但仅有调度器还不足以支持相应的批量计算作业，作为一个批量计算系统还需要其它很多组件的支持，
例如 作业管理，数据管理，资源规划等等。Volcano 作为一个批量计算系统，包括了 调度器，作业管理，数据管理等组件在内的所有必须组件，以提供端到端的批量计算系统。

Volcano作为一个平台，允许把现在的各种框架的各种计算任务作为Kubernetes的工作负载运行，映射的是Volcano的CRD，实际创建的是Pod或Job。

可以把Spark、Flink、TensorFlow、PyTorch等任务接入Volcano，当然实际是不是这样最好，那另说，只是可以接。


参考文档：  
https://github.com/volcano-sh/volcano
https://volcano.sh/en/
http://www.klaus1982.cn/tech/2019/10/05/volcano_faq_1/