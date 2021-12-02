package jvm.classfile.constantpool.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.ConstantInfo;
import jvm.classfile.constantpool.ConstantPool;

import java.util.Map;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class ConstantMemberRefInfo implements ConstantInfo {

    protected ConstantPool constantPool;
    protected int classIdx;
    private int nameAndTypeIdx;

    ConstantMemberRefInfo(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        this.classIdx = reader.readUnit16();
        this.nameAndTypeIdx = reader.readUnit16();
    }

    @Override
    public int tag() {
        return 0;
    }

    public String className() {
        return this.constantPool.getClassName(this.classIdx);
    }

    public Map<String, String> nameAndDescriptor() {
        return this.constantPool.getNameAndType(this.nameAndTypeIdx);
    }

}
