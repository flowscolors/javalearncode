
基本原理是使用注解或xml文件配置SQL，Mybatis使用BoundSql去上述地方获取SQL，等到真正执行时，在SimpleExecutor中把SQL、参数、数据库连接传入；
拼接成真正执行的SQL，往数据库连接发，得到数据。  


参考文档：  
https://github.com/HelloZzzzz/tiny_mybatis
https://bugstack.cn/itstack-demo-any/2019/12/25/%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90-Mybatis%E6%8E%A5%E5%8F%A3%E6%B2%A1%E6%9C%89%E5%AE%9E%E7%8E%B0%E7%B1%BB%E4%B8%BA%E4%BB%80%E4%B9%88%E5%8F%AF%E4%BB%A5%E6%89%A7%E8%A1%8C%E5%A2%9E%E5%88%A0%E6%94%B9%E6%9F%A5.html
https://bugstack.cn/itstack-demo-any/2020/01/13/%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90-%E5%9F%BA%E4%BA%8Ejdbc%E5%AE%9E%E7%8E%B0%E4%B8%80%E4%B8%AADemo%E7%89%88%E7%9A%84Mybatis.html