package springframework.beans.factory;

/**
 * @author flowscolors
 * @date 2021-11-13 17:58
 */
//实现此接口，既能感知到所属的 ClassLoader
public interface BeanClassLoaderAware extends Aware {

    void setBeanClassLoader(ClassLoader classLoader);

}
