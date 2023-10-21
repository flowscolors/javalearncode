


## 1. callable()底层原理

## 2. 实现线程callable()的特殊工具类

PrivilegedCallable
PrivilegedCallableUsingCurrentClassLoader
RunnableAdapter
Task.TaskCallable

## 3. Future接口、.RunnableFuture接口、.FutureTask类底层原理
Future表示一个任务的生命周期，并提供了相应的方法来判断是否已经完成或取消，以及获取任务的结果和取消任务。

future.get（） 如果任务已完成，则get会立即返回或抛出一个Exception。如果任务尚未完成，那么get会阻塞并直到任务完成。
get还可以设置时间，如果规定时间内等不到返回则执行其他任务补救。