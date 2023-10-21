

Velero 是一个开源工具，用于安全备份和恢复、执行灾难恢复以及迁移 Kubernetes 集群资源和持久卷

旨在帮助：
灾难恢复：在基础设施丢失、数据损坏和/或服务中断的情况下减少恢复时间。
数据迁移：通过轻松地将 Kubernetes 资源从一个集群迁移到另一个集群，实现集群可移植性。
数据保护：提供关键数据保护功能，例如计划备份、保留计划以及用于自定义操作的备份前或备份后挂钩。


工作原理：
1.使用crd定义需要备份的对象。用户提交crd(1)之后，BackupController 控制器检测到生成的备份对象时（2）执行备份操作（3）。
2.将备份的集群资源和存储卷快照上传到 Velero 的后端存储（4）和（5）。
3.执行还原操作时，Velero 会将指定备份对象的数据从后端存储同步到 Kubernetes 集群完成还原工作。中间可自定义hook在恢复前后执行某些操作。

对于Kubernetes资源，很简单，都变成yaml去其他集群部署就可以了。问题在于持久化的内容，如Local PV、NFS PVC。
这部分从 Velero 1.5版本开始，Velero 默认使用 Restic 备份所有 Pod 卷，而不必单独注释每个 Pod。Restic可以把某个需要备份的文件、目录 备份到本地或远程某个服务器上（可以是SFTP、REST Servre、S3、Minio等）


参考文档：  
https://velero.io/
https://restic.net/
https://github.com/vmware-tanzu/velero
https://www.sklinux.com/posts/devops/%E9%AB%98%E6%95%88%E5%A4%87%E4%BB%BD%E5%B7%A5%E5%85%B7restic%E6%8E%A8%E8%8D%90/
https://kaichu.io/posts/velero-research-practice/
https://cloud.tencent.com/document/product/457/52331