package jvm.classpath.impl;

import jvm.classpath.Entry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author flowscolors
 * @date 2021-11-09 0:13
 */
//目录形式类路径
public class DirEntry implements Entry {

    //完全路径
    private Path absolutePath;

    public DirEntry(String path){
        //构造函数运行 获取相对路径
        this.absolutePath = Paths.get(path).toAbsolutePath();
    }

    @Override
    public String toString() {
        return this.absolutePath.toString();
    }

    //通过java核心类去读取对应class文件
    @Override
    public byte[] readClass(String className) throws Exception {
        return Files.readAllBytes(absolutePath.resolve(className));
    }
}
