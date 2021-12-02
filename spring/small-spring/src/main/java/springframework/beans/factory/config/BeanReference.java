package springframework.beans.factory.config;

/**
 * @author flowscolors
 * @date 2021-11-08 13:49
 */
public class BeanReference {
    private final String beanName;

    public String getBeanName() {
        return beanName;
    }

    public BeanReference(String beanName) {
        this.beanName = beanName;
    }
}
