

Knative 基于 Kubernetes 的平台，用于部署和管理现代无服务器工作负载。主要有两部分，Knative Serving，Knative Event

Knative Serving 项目提供了中间件原语，可实现：

* 无服务器容器的快速部署
* 自动缩放到零
* 路由和网络编程
* 已部署代码和配置的时间点快照

Knative Event 项目作为一个事件系统，旨在满足云原生开发的常见需求。通过从任何地方传递事件来实现异步应用程序开发。

服务在开发过程中松散耦合，独立部署
生产者可以在消费者收听之前生成事件，消费者可以表达对尚未生成的事件或事件类的兴趣。
可以连接服务以创建新的应用程序
不修改生产者或消费者，以及
能够从特定的生产者中选择特定的事件子集。

优点是参与者很多，社区活跃，文档全面，对于社区里的各种语言的使用都有例子 Java、Go、Python https://knative.dev/docs/samples/eventing/

并没有自身控制台页面，部署策略需要使用kubectl apply crd。云厂商提供的knative服务中有部分集成了自己的控制台。

参考文档：  
https://github.com/knative/serving
https://knative.dev/docs/