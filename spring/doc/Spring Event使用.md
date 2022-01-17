
Spring Event

## 涉及组件

1. ApplicationEvent 属于真正用来传递的消息，可以被继承,一般会自定义自己的Event。
```java
public class InitEvent extends ApplicationEvent{ 
    pubic InitEvent(Object object) {super(object);}
}

public class RefreshEvent extends ApplicationEvent{ 
    pubic RefreshEvent(Object object) {super(object);}
}
```

2. ApplicationEventPublisher 信息发送者，业务一般调用该类的publishEvent发送事件。applicationContext也有该方法。
```text
public static void publishEvent(ApplicationEvent event){
    if(applicationContext == null){
        return;
    }
     applicationContext.publishEvent(event);
}
```


### 使用案例
首先是Spring中就大量使用了Spring Event，比如启动的main函数中，就会发送Event。如果Spring类加载有问题，会通过Event发生错误事件。

```Text
Application events are sent in the following order, as your application runs:

1、An ApplicationStartedEvent is sent at the start of a run, but before any processing except the registration of listeners and initializers.


    ApplicationStartedEvent在任何处理之前，程序开始运行时被发送，初始化和自定义注册监听事件除外

2、An ApplicationEnvironmentPreparedEvent is sent when the Environment to be used in the context is known, but before the context is created.

    ApplicationEnvironmentPreparedEvent在上下文被创建之前，应用环境被已知的上下文环境中使用时被发送

3、An ApplicationPreparedEvent is sent just before the refresh is started, but after bean definitions have been loaded.

    ApplicationPreparedEvent在刷新开始之前，beans加载之后被发送

4、An ApplicationReadyEvent is sent after the refresh and any related callbacks have been processed to indicate the application is ready to service requests.

    ApplicationReadyEvent在刷新后被发送，并且任何相关的回调都已经被处理，表明该应用程序已经准备好处理服务请求

5、An ApplicationFailedEvent is sent if there is an exception on startup.

    ApplicationFailedEvent如果启动时存在异常时被发送

You often won’t need to use application events, but it can be handy to know that they exist. Internally, Spring Boot uses events to handle a variety of tasks.

我们不需要使用应用程序事件，但是很方便的知道他们存在，在SpringBoot内部使用各种事件来处理各种任务
```



参考文档：
https://www.jianshu.com/p/f0a968cfb331
