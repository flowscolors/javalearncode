## 1.arthas工具介绍


常用基本命令：
dashboard
jvm
thread  查看线程状态 可指定--state
jad  反编译指定已加载类的源码
sc  查看jvm已加载的类信息  sc -d ClassName
sm  查看已加载类的方法信息  sm -d ClassName method
classloader 
watch 方法执行数据观测，观察入参、方法调用
monitor  
tt  记录下指定方法每次调用的入参和返回信息并能对这些不同的时间下调用进行观测


## 2.使用场景与对应命令

确定某个方法实际调用耗时,默认会展示最近100次调用。可指定多个方法进行监测。
trace ClassName method

对jvm生成火焰图  
profiler start --event cpu --duration 300

加载外部class文件到jvm中  
redefine  
redefine -p d://tmp/Test.class //加载指定位置的class文件  
redefine -c 256a485d -p d://tmp/Test.class //让指定的classloader来加载，256a485d为该classloader的hashcode，可通过classloader命令来查得
但是需要注意的是：
不允许新增加field/method

正在跑的函数，没有退出不能生效，比如下面新增加的System.out.println，只有run()函数里的会生效

参考文档：  
https://arthas.aliyun.com/doc/tt.html