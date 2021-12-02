package springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author flowscolors
 * @date 2021-11-13 12:39
 */
public interface Resource {

    InputStream getInputStream() throws IOException;
}
