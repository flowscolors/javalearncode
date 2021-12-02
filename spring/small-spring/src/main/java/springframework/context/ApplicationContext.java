package springframework.context;

import springframework.beans.factory.HierarchicalBeanFactory;
import springframework.beans.factory.ListableBeanFactory;
import springframework.core.io.ResourceLoader;

/**
 * @author flowscolors
 * @date 2021-11-13 13:57
 */
//应用上下文 实际上继承了ListableBeanFactory，也是一种类型的BeanFactory
public interface ApplicationContext extends ListableBeanFactory , HierarchicalBeanFactory, ResourceLoader, ApplicationEventPublisher{
}
