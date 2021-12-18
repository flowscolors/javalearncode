
Crossplane 是一个开源 Kubernetes 附加组件，可将您的集群转换为通用控制平面。Crossplane 使平台团队能够组装来自多个供应商的基础设施，并公开更高级别的自助服务 API 供应用程序团队使用，而无需编写任何代码。

Crossplane 扩展了您的 Kubernetes 集群以支持编排任何基础架构或托管服务。将 Crossplane 的细粒度资源组合成更高级别的抽象，可以使用您最喜欢的工具和现有流程对其进行版本控制、管理、部署和使用。将 Crossplane 安装 到任何 Kubernetes 集群以开始使用。


Kubernetes并没有提供一个开箱即用的PaaS服务，但是其基于CRD的声明式拓展机制、清晰的API和良好的抽象让它成为了完美的基础组件层。

虽然已经有了很多商业PaaS出现，但是他们大多只能满足80%的需求，大部分企业内部还是有自己的PaaS团队完善企业自己的需求。可以认为每个工程团队都需要一个PaaS平台。

其实类似于CRD，只是又抽象了一层，不走Kubernetes的控制循环，而是提供了自己的调节循环。Setup、Connect、Create、Observe、Update、Delete等。实际还是要自己开发资源的。



顺便阿里的OAM想成为一种新的容器运行标准（阿里野心不小），但是一直缺少最关键的运行时，Crossplane补上了最后一块空缺，可以说两个开源项目关联很大。
顺便Crossplane的中文文档也十分的少···

参考文档：
https://github.com/crossplane/crossplane
https://crossplane.io/
https://cloudnative.to/blog/oam-crossplane/
https://cloud.tencent.com/developer/article/1752312
https://github.com/crossplane/crossplane/blob/baa18e348e3680accba8056475263b8301476986/docs/contributing/provider_development_guide.md
https://blog.csdn.net/xialingming/article/details/120304088
https://blog.csdn.net/xialingming/article/details/120484509