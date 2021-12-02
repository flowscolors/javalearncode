package springframework.beans.factory.config;

/**
 * @author flowscolors
 * @date 2021-11-07 22:30
 */
//获取单例接口
public interface SingletonBeanRegistry {
    Object getSingleton(String name);

    void registerSingleton(String beanName, Object singletonObject);
}
