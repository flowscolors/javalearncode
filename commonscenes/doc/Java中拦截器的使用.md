

## Spring MVC 拦截器
首先Spring的拦截器一般只需要我们实现方法即可，常用的有以下接口。需要注意的是这些拦截器底层原理还是基于Java的动态代理实现。

HandlerInterceptor接口，内含preHandle()、postHandle()和afterCompletion()三个方法。分别在请求前、渲染前、返回前做操作。

AsyncHandlerInterceptor接口，在HandlerInterceptor基础上新增afterConcurrentHandlingStarted()方法。
HandlerInterceptorAdapter抽象类，继承AsyncHandlerInterceptor接口的同时，又复写了preHandle方法。

WebRequestInterceptor接口，内含preHandle()、postHandle()和afterCompletion()三个方法。而且这 3 个方法都传递了同一个参数WebRequest。

Interceptor接口，内含init()、destroy()和intercept()三个方法。分别是初始化操作、释放资源、实现拦截功能。
AbstractInterceptor实现了Interceptor接口，并且实现了空的init()和destroy()方法，直接复写intercept()方法就可以了。

所以实际使用就是使用XML或者Java Config进行相关配置。拦截器可以构成一个拦截器链，按顺序先声明先执行。

## SpringCloud Gateway拦截器
这里其实是过滤器，因为SpringCloud GateWay上所有的功能都可以说是基于过滤器实现的。

SpringCloud基于webflux实现，本身内部有GlobalFilter，用户也可定义自己的Filter，implements GlobalFilter, Ordered即可。

SpringCloud GateWay中的GatewayFilter、GlobalFilter和过滤器链GatewayFilterChain，都依赖到ServerWebExchange

这里的设计和Servlet中的Filter是相似的，当前过滤器可以决定是否执行下一个过滤器的逻辑，由GatewayFilterChain#filter()是否被调用来决定。而ServerWebExchange就相当于当前请求和响应的上下文。

ServerWebExchange实例不单存储了Request和Response对象，还提供了一些扩展方法，如果想实现改造请求参数或者响应参数，可以了解ServerWebExchange。

参考文档： https://www.cnblogs.com/fdzang/p/11812348.html
https://segmentfault.com/a/1190000021135485

## Kafka拦截器
Kafka拦截器有两种，生产者拦截器和消费者拦截器。
生产者拦截器可以在消息发送前做一些工作，如过滤消息、修改消息内容、定制回调策略、统计、metrics等。

使用起来，只要实现 org.apache.kafak.client.producer.ProducerInterceptor 接口中的方法即可。

并且生产者可以指定多个拦截器以形成拦截链，拦截链会按照interceptor.classer参数配置的顺序一一执行。

消费者拦截器，需要实现 org.apache.kafka.clients.consumer.ConsumerInterceptor 接口，主要是 onConsume、onCommit接口。