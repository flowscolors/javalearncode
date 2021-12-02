package jvm.classpath;

import jvm.classpath.impl.WildcardEntry;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author flowscolors
 * @date 2021-11-08 23:53
 */
//classpath 类路径
public class Classpath {

    private Entry bootstrapClasspath;  //启动类路径
    private Entry extensionClasspath;  //扩展类路径
    private Entry userClasspath;       //用户类路径

    //初始化方法，需要传入jre参数和classpath参数，用来加载bootstrap extension的类，和用户提供的classpath下的类
    public Classpath(String jreOption, String cpOption) {
        //启动类&扩展类 "C:\Program Files\Java\jdk1.8.0_161\jre"
        bootstrapAndExtensionClasspath(jreOption);
        //用户类 E:\..\org\itstack\demo\test\HelloWorld
        parseUserClasspath(cpOption);
    }

    //加载jre/lib/* 和 jre/lib/ext/* 下的类
    private void bootstrapAndExtensionClasspath(String jreOption) {

        String jreDir = getJreDir(jreOption);

        //..jre/lib/*
        String jreLibPath = Paths.get(jreDir, "lib") + File.separator + "*";
        bootstrapClasspath = new WildcardEntry(jreLibPath);

        //..jre/lib/ext/*
        String jreExtPath = Paths.get(jreDir, "lib", "ext") + File.separator + "*";
        extensionClasspath = new WildcardEntry(jreExtPath);

    }

    //获得本机jre路径
    private static String getJreDir(String jreOption) {
        if (jreOption != null && Files.exists(Paths.get(jreOption))) {
            return jreOption;
        }
        if (Files.exists(Paths.get("./jre"))) {
            return "./jre";
        }
        String jh = System.getenv("JAVA_HOME");
        if (jh != null) {
            return Paths.get(jh, "jre").toString();
        }
        throw new RuntimeException("Can not find JRE folder!");
    }

    //获得classpath路径
    private void parseUserClasspath(String cpOption) {
        if (cpOption == null) {
            cpOption = ".";
        }
        userClasspath = Entry.create(cpOption);
    }

    //传入类名 返回字节数组
    public byte[] readClass(String className) throws Exception {
        className = className + ".class";

        //[readClass]启动类路径
        try {
            return bootstrapClasspath.readClass(className);
        } catch (Exception ignored) {
            //ignored
        }

        //[readClass]扩展类路径
        try {
            return extensionClasspath.readClass(className);
        } catch (Exception ignored) {
            //ignored
        }

        //[readClass]用户类路径
        return userClasspath.readClass(className);
    }

}
