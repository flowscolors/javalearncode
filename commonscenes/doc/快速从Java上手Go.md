
为什么Docker/Kubernetes选择Go？
可能有两个原因：
1.Go的特性让它更适合中间件开发，而Docker/Kubernetes都是云环境中基础设施的存在，必须考虑占用最少的资源，将资源留给业务，所以要求性能高，占用少。
具体特性有：静态类型语言，编译完不需要运行时依赖。执行性能和开发效率的平衡，都不是最好，但都是Top。天生为并行计算设计的线程模型。

2.因为已经出现用Go写的Docker/Kubernetes，导致生态圈已经形成，轮子多，则后面的人更会使用Go做开发。
轮子多是很重要的。Python有人工智能、量化分析的库，Java有各种业务的库，Go有云原生的轮子，对于一个普通的开发者，没有轮子自己造是很花时间的。
因为技术之所以存在并不断发展，是因为业务有需要，根据业务的不同，我们往往选择最适合业务需求的技术，而不是使用一种技术解决所有问题。如果这样就本末倒置了。



参考文档：  
https://go.dev/doc/tutorial/getting-started
https://cgiirw.github.io/2018/11/17/go-learning03/
https://www.luozhiyun.com/archives/206
https://gobyexample.com/
https://github.com/QingyaFan/container-cloud/issues/6