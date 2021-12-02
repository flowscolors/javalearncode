package springframework.beans.factory;

import springframework.beans.BeansException;

/**
 * @author flowscolors
 * @date 2021-11-07 22:26
 */
//已经存在的 Bean 工厂接口用于获取 Bean 对象，可以get不同范围的bean，对bean生命周期做各种配置
public interface BeanFactory {
    Object getBean(String name) throws BeansException;

    Object getBean(String name,Object... args) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;
}
