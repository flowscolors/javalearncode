
Prometheus作为一种基于metrics的监控机制，优势在于强大的PromQL，提供了灵活的数据分析查询能力，结合Grafana提高的仪表盘能力，便于查询和展示。

对于Kubernetes集群来说，单集群内部的高可用Prometeus最后肯定是无法满足要求，60多个集群的Prometheus势必要统一入口。
优化方案：
1.使用Prometheus 联邦机制，Thanos方案。

2.将实际数据存储到远端的时序数据库，而非存储到本地文件中。并且需要解决随着数据量增多的查询、告警性能。

3.对于某些场景可能需要优化PromQL，提供更多场景的算子。

4.引入Flink流式计算，承接原本由存储服务支持的预计算，阈值告警检测能力。实现ALter Rule计算告警能力。











参考文档：
https://mp.weixin.qq.com/s/-yt29HhgDOHrGhUVga-lhw