package springframework.beans.factory.config;

import springframework.beans.factory.HierarchicalBeanFactory;

/**
 * @author flowscolors
 * @date 2021-11-13 12:53
 */
//一个可获取 BeanPostProcessor、BeanClassLoader等配置化的接口。
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
    /**
     * 销毁单例对象
     */
    void destroySingletons();

}
