package springframework.beans.factory;

/**
 * @author flowscolors
 * @date 2021-11-13 16:56
 */
public interface DisposableBean {

    void destroy() throws Exception;
}
