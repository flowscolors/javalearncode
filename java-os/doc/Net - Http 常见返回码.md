## 有关Http常用返回码




### 403 重定向
在使用nginx时经常会遇到这种问题，包括从3层 4层做转发的问题



### 404 找不到网页
基本就是后端没起起来问题了


### 502 
1.一般是nginx给出的报错，常见于nginx+tomcat的场景下，nginx没法转发到后端tomcat。一般此时查看nginx日志有大量no live upstream while connecting to upstream.即nginx找不到活着的tomcat节点了。  
此种情况一般需要nginx修改配置，添加max_fails和fail_timeout参数。tomcat也需要调整最大响应时间，比如设置proxy_connect_timeout=1，proxy_connect_timeout=30。  

参考链接: https://stackoverflow.com/questions/169453/bad-gateway-502-error-with-apache-mod-proxy-and-tomcat

2.使用harbor过程中遇到该问题，调用harbor接口异常导致的502报错。表现为harbor-core容器异常重启。