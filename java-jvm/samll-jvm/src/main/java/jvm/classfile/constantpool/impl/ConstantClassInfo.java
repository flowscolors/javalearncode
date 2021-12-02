package jvm.classfile.constantpool.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.ConstantInfo;
import jvm.classfile.constantpool.ConstantPool;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class ConstantClassInfo implements ConstantInfo {

    public ConstantPool constantPool;
    public int nameIdx;

    public ConstantClassInfo(ConstantPool constantPool) {
         this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
         this.nameIdx = reader.readUnit16();
    }

    public String name(){
        return this.constantPool.getUTF8(this.nameIdx);
    }

    @Override
    public int tag() {
        return this.CONSTANT_TAG_CLASS;
    }

}
