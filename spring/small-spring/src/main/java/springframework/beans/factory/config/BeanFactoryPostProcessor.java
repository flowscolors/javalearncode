package springframework.beans.factory.config;

import springframework.beans.BeansException;
import springframework.beans.factory.ConfigurableListableBeanFactory;

/**
 * @author flowscolors
 * @date 2021-11-13 14:05
 */
//BeanFactoryPostProcessor 允许自定义修改 BeanDefinition 属性信息的拓展点
public interface BeanFactoryPostProcessor {

    /**
     * 在所有的 BeanDefinition 加载完成后，实例化 Bean 对象之前，提供修改 BeanDefinition 属性的机制
     *
     * @param beanFactory
     * @throws BeansException
     */
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
