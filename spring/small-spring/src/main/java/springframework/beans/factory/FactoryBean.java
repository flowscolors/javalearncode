package springframework.beans.factory;

/**
 * @author flowscolors
 * @date 2021-11-13 20:29
 */
public interface FactoryBean<T> {

    T getObject() throws Exception;

    Class<?> getObjectType();

    boolean isSingleton();

}
