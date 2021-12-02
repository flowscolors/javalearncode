package springframework.core.io;

/**
 * @author flowscolors
 * @date 2021-11-13 12:40
 */
public interface ResourceLoader {

    /**
     * Pseudo URL prefix for loading from the class path: "classpath:"
     */
    String CLASSPATH_URL_PREFIX = "classpath:";

    Resource getResource(String location);
}
