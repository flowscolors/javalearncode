


###  INSERT  ON DUPLICAT
基本命令实现幂等，如果有则更新，无则插入。
需要注意该条SQL在高并发的情况下会有锁争用的问题。

https://www.cnblogs.com/dongruiha/p/6780447.html  
https://cloud.tencent.com/developer/article/1004900

__
### MYSQL命令结果输出到文件
* 直接指向命令，需要对应目录权限
* 在外面使用mysql配合>输出到文件

参考文档：  
https://www.cnblogs.com/emanlee/p/4233602.html