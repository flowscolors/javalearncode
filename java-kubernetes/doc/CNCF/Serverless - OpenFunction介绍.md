
OpenFunction是一个云原生开源 FaaS（功能即服务）平台，旨在让用户能够专注于他们的业务逻辑，而无需担心底层运行环境和基础设施。用户只需要以函数的形式提交与业务相关的源代码。


OpenFunction 具有但不限于以下功能：
* 将业务相关的功能源代码转化为可运行的应用源代码。
* 从转换后的应用程序源代码生成可部署的容器映像。
* 将生成的容器镜像部署到K8s等底层运行环境，根据业务流量自动伸缩，无流量时伸缩为0。
* 为触发器功能提供事件管理功能。
* 提供额外的功能来管理功能版本、入口管理等。

国内青云开源，基本大部分基于开源实现，当你打开实例yaml时引入眼帘的就是dapr。
> OpenFunction 项目引用了很多第三方的项目，如 Knative、Tekton、ShipWright、Dapr、KEDA 等，手动安装较为繁琐，推荐使用 Prerequisites 文档 中的方法，一键部署 OpenFunction 的依赖组件。
> sh hack/deploy.sh --with-shipwright --with-openFuncAsync --poor-network

目前看似乎并没有提供一个web ui来给用户提交函数。而是让用户把函数上传到代码仓如github，crd会去从代码仓拉代码进行打包部署操作。

另外从写代码到写函数，还是某种特定场合下的函数，对开发者来说也是有学习成本的。



参考文档：  
https://github.com/OpenFunction/OpenFunction
https://kubesphere.io/zh/blogs/serverless-way-for-kubernetes-log-alert/