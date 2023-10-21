
老牌的Zuul本质是基于Spring MVC框架开发的Web Servlet。Zuul核心模块是一系列filter过滤器，使用阻塞性IO，通过线程池技术实现请求的并发处理。
Zuul2开始使用NIO基于Reactor模式进行请求处理。

## 1.接受请求 ReactorHttpHandlerAdapter
Spring Cloud Gateway的底层框架是netty，接受请求的关键类是ReactorHttpHandlerAdapter，做的事情很简单，
就是将netty的请求、响应转为http的请求、响应并交给一个http handler执行后面的逻辑，下图为该类的源码仅保留核心逻辑。

```shell script
 @Override
    public Mono< Void> apply(HttpServerRequest request, HttpServerResponse response) {
        NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(response.alloc());
        ServerHttpRequest adaptedRequest;
        ServerHttpResponse adaptedResponse;
    //转换请求
        try {
            adaptedRequest = new ReactorServerHttpRequest(request, bufferFactory);
            adaptedResponse = new ReactorServerHttpResponse(response, bufferFactory);
        }
        catch (URISyntaxException ex) {
            if (logger.isWarnEnabled()) {
            ...
        }
        ...
        return this.httpHandler.handle(adaptedRequest, adaptedResponse)
                .doOnError(ex -> logger.warn("Handling completed with error: " + ex.getMessage()))
                .doOnSuccess(aVoid -> logger.debug("Handling completed with success"));
    }
```

## 2.WEB过滤器链 
http handler做的事情第一是将request 和 response转为一个exchange，这个exchange非常核心，是各个filter之间参数流转的载体，
该类包含request、response、attributes(扩展字段)，接着做的事情就是web filter链的执行，其中的逻辑主要是监控。

其中WebfilterChainParoxy 又会引出新的一条filter链，主要是安全、日志、认证相关的逻辑，由此可见Spring Cloud Gateway的过滤器设计是层层嵌套，扩展性很强。


## 3.寻找路由规则 RoutePredicateHandlerMapping
核心类是RoutePredicateHandlerMapping，逻辑也非常简单，就是把所有的route规则的predicate遍历一遍看哪个predicate能够命中，核心代码是：
```shell script
return this.routeLocator.getRoutes()
      .filter(route -> {
         ...
         return route.getPredicate().test(exchange);
      })
```
常用代码使用的是path进行过滤，所以背后的逻辑是PathRoutePredicateFactory来完成的，除了PathRoutePredicateFactory还有很多predicate规则。


## 4.核心过滤器链执行  FilteringWebHandler
找到路由规则后下一步就是执行了，这里的核心类是FilteringWebHandler，其中的源码为：


做的事情很简单：
1.获取route级别的过滤器
2.获取全局过滤器
3.两种过滤器放在一起并根据order进行排序
4.执行过滤器链

所以实际哪怕你只配了一个route级别的filter，gateway也会给你加上很多全局级别的filter。这些过滤器的功能主要是url解析，请求转发，响应回写等逻辑，因为我们这里用的是forward schema，所以请求转发会由ForwardRoutingFilter进行执行。

## 5.请求转发  ForwardRoutingFilter
ForwardRoutingFilter做的事情也很简单，直接复用了spring mvc的能力，将请求提交给dispatcherHandler进行处理，dispatcherHandler会根据path前缀找到需要目标处理器执行逻辑。
使用不同的Filter可以实现不同的功能，如http调用，grpc调用。


## 6.响应回写  NettyWriteResponseFilter
响应回写的核心类是NettyWriteResponseFilter，但是大家可以注意到执行器链中NettyWriteResponseFilter的排序是在最前面的，按道理这种响应处理的类应该是在靠后才对，这里的设计比较巧妙。大家可以看到chain.filter(exchange).then()，意思就是执行到我的时候直接跳过下一个，等后面的过滤器都执行完后才执行这段逻辑，这种行为控制的方法值得学习。


## 总结

1.过滤器是Spring Cloud Gateway最核心的设计，甚至于可以夸张说Spring Cloud Gateway是一个过滤器链执行框架而不是一个API网关，因为API网关实际的请求转发、请求响应回写都是在过滤器中做的，这些是Spring Cloud Gateway感知不到的逻辑。

2.Spring Cloud Gateway路由规则获取的模块具备优化的空间，因为是循环遍历进行获取的，如果每个route规则较多，predicate规则较复杂，就可以考虑用map进行优化了，当日route规则，predicate规则也不会很复杂，兼顾到代码的可读性，当前方式也没有什么问题。

3.作为API网关框架，内置了非常多的过滤器，如果有过滤器的卸载功能可能会更好，用户可用根据实际情况卸载不必要的功能，背后减少的逻辑开销，在调用量极大的API网关场景，收益也会很可观。

4.实际我们使用的时候拓展Gateway的功能也是使用过滤器，比如Redis限流、鉴权等。

参考文档：
https://developer.aliyun.com/article/804348