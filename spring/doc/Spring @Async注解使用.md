
基于AOP实现，可以让用户使用注解的方法把方法放到线程池中执行。

注意的点：
1. 开启需要在启动类上添加 @EnableAsync 注解，在方法上添加 @Async 注解
2. 默认的线程池有OOM的风险
3. 被@Async修饰的方法，返回值只能是 void 或者 Future 类型，否则即使返回了其他值，不会报错，但是返回的值是 null，有空指针风险。
4. @Async 注解中有一个 value 属性，看注释应该是可以指定自定义线程池的。实际使用的时候value指定bean名称，会从spring的bean里面找，即可使用对应线程池进行任务执行。内部有一个map存方法和线程池映射关系。
5. 默认线程池是一个 coreSize 为8 ，maxSize 和 队列长度都是 Integer.MAX_VALUE
6. 



参考文档：  
https://www.chuckfang.com/2019/11/13/Async/  
https://www.codehome.vip/archives/springboot-async