package springframework.context;

import springframework.beans.BeansException;

/**
 * @author flowscolors
 * @date 2021-11-13 18:00
 */
//实现此接口，既能感知到所属的 ApplicationContext
public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext) throws BeansException;
}
