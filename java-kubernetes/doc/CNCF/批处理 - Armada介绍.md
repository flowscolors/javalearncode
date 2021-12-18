
并不是华为开源的Karmada，而是一家英国公司G-Research开源的armada。

Armada 是一个用于在 Kubernetes 集群上调度和运行批处理作业（例如，用于训练机器学习模型的计算作业）的系统。Armada 旨在实现高可用性并在数千个节点上处理每秒数百个作业的调度（可能有数百万个作业排队）。

为了实现这一点，与之前的 Kubernetes 批处理调度器（例如kube-batch）不同，Armada可以同时在多个 Kubernetes 集群上调度作业，从而超出单个 Kubernetes 集群（我们发现大约 1000 个节点）的限制。此外，Kubernetes 集群可以在不中断的情况下与 Armada 动态连接和断开连接。作业提交到作业队列，其中可能有很多（例如，每个用户可以有一个单独的队列），Armada 在队列之间公平分配计算资源。

Armada 松散地基于HTCondor批处理调度程序，可以用作 HTCondor 的替代品，前提是所有节点都注册到 Kubernetes 集群中。

Armada不是
* 服务调度程序（即 Armada 作业应该有一个有限的生命周期）
* 专为低延迟调度而设计（预计作业提交后 10 秒左右）
* 旨在在 Kubernetes 集群以外的底层系统上调度作业


在多个Kubernetes集群外部运行一个Armada调度平台，在每个集群内部部署一个调度Executor组件，监控集群现在状态(CPU、GPU、内存)，和调度平台通信，

根据结果往ApiServer创建Pod，并将进度报告给服务端组件。调度平台还有一个UI，能让用户更加容易可视化工作在集群中的流动。

是的，Armada其实更类似于Volcano，两者都是做批量计算系统，有单独的调度器，作业管理，数据管理，只是Volcano 可能做的多一些，并且基于CRD开发，拓展性更多。



参考文档：  
https://github.com/G-Research/armada
https://cloud.tencent.com/developer/article/1791694
https://github.com/kubernetes-sigs/kube-batch