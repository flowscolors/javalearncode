package jvm.classpath;

import jvm.classpath.impl.CompositeEntry;
import jvm.classpath.impl.DirEntry;
import jvm.classpath.impl.WildcardEntry;
import jvm.classpath.impl.ZipEntry;

import java.io.File;

/**
 * @author flowscolors
 * @date 2021-11-09 0:07
 */
//类路径接口
public interface Entry {

    byte[] readClass(String className) throws Exception;

    //对Win、Linux 四种情况下的类文件路径进行分析
    static Entry create(String path){
        //File.pathSeparator；路径分隔符(win\linux)
        if (path.contains(File.pathSeparator)) {
            return new CompositeEntry(path);
        }

        if (path.endsWith("*")) {
            return new WildcardEntry(path);
        }

        if (path.endsWith(".jar") || path.endsWith(".JAR") ||
                path.endsWith(".zip") || path.endsWith(".ZIP")) {
            return new ZipEntry(path);
        }

        return new DirEntry(path);

    }

}
