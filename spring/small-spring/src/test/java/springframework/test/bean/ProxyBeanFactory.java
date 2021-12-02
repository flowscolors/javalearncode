package springframework.test.bean;

import springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-11-13 20:38
 */
public class ProxyBeanFactory implements FactoryBean<IUserDao> {


    //ProxyBeanFactory 相当于代理类实现了代理了dao层的接口，这里执行了queryUserName方法
    @Override
    public IUserDao getObject() throws Exception {
        InvocationHandler handler = (proxy, method, args) -> {
            // 添加排除方法
            if ("toString".equals(method.getName())) return this.toString();

            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("10001", "jokerProxy");
            hashMap.put("10002", "kingproxy");
            hashMap.put("10003", "zeroProxy");

            return "你被代理了 " + method.getName() + "：" + hashMap.get(args[0].toString());
        };
        return (IUserDao) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{IUserDao.class}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return IUserDao.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
