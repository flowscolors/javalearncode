

Cluster API是一个Kubernetes项目，它将声明式Kubernetes风格的API用于集群的创建、配置和管理。它通过使用时CustomResourceDefinitions（CRDs）来扩展被Kubernetes API Server暴露的API来实现这些功能，
从而允许用户创建新资源，例如集群（指Kubernetes集群）和Machine（指组成集群的节点的Machine）。然后每个资源的控制器负责对这些资源的更改做出反应，以启动集群。API的设计可以让不同的基础架构提供程序可以与其集成，进而提供针对其环境的特定逻辑。


很明显对于阿里云、腾讯云上的集群，我们可以直接使用clusterAPI调用云厂商的接口去创建一台服务器，而对于物理机则需要我们自己维护一套节点池以及接入接口。











参考文档：
https://github.com/kubernetes-sigs/cluster-api-provider-nested
https://cluster-api.sigs.k8s.io/
https://cluster-api.sigs.k8s.io/user/quick-start.html
https://docs.google.com/document/d/1LdooNTbb9PZMFWy3_F-XAsl7Og5F2lvG3tCgQvoB5e4/edit#
https://segmentfault.com/a/1190000022650813