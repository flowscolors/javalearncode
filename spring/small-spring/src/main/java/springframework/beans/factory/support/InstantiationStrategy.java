package springframework.beans.factory.support;

import springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

/**
 * @author flowscolors
 * @date 2021-11-07 23:57
 */
public interface InstantiationStrategy {
    Object instantiate(BeanDefinition beanDefinition, String beanName, Constructor ctor,Object[] args) throws Exception;
}
