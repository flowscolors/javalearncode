
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


