package springframework.beans.factory;

import springframework.beans.BeansException;

/**
 * @author flowscolors
 * @date 2021-11-13 17:59
 */
//实现此接口，既能感知到所属的 BeanFactory
public interface BeanFactoryAware extends Aware {

    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
