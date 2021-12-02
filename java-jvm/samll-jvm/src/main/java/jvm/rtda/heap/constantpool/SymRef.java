package jvm.rtda.heap.constantpool;

import jvm.rtda.heap.methodarea.Class;

public class SymRef {

    public RunTimeConstantPool runTimeConstantPool;
    public String className;
    public Class clazz;

    public Class resolvedClass() {
        if (null != this.clazz) return this.clazz;
        Class d = this.runTimeConstantPool.getClazz();
        Class c = d.loader.loadClass(this.className);;
        this.clazz = c;
        return this.clazz;
    }

}
