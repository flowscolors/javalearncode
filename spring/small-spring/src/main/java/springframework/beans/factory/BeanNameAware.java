package springframework.beans.factory;

/**
 * @author flowscolors
 * @date 2021-11-13 17:59
 */
public interface BeanNameAware extends Aware {

    void setBeanName(String name);
}
