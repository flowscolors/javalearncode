package springframework.context;

import springframework.beans.BeansException;

/**
 * @author flowscolors
 * @date 2021-11-13 13:58
 */
//可配置的应用上下文 在原本的基础上实现了更多的功能，比如刷新其中的bean容器
public interface ConfigurableApplicationContext extends ApplicationContext {

    /**
     * 刷新容器
     *
     * @throws BeansException
     */
    void refresh() throws BeansException;

    void registerShutdownHook();

    void close();
}
