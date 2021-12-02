package springframework.beans.factory;

import springframework.beans.factory.BeanFactory;

/**
 * @author flowscolors
 * @date 2021-11-13 12:55
 */
//在 Spring 源码中它提供了可以获取父类 BeanFactory 方法，属于是一种扩展工厂的层次子接口。
public interface HierarchicalBeanFactory extends BeanFactory {
}
