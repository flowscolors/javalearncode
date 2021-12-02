package jvm.classfile.constantpool.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.ConstantInfo;
import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.ConstantInfo;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class ConstantMethodHandleInfo implements ConstantInfo {

    private int referenceKind;
    private int referenceIndex;

    @Override
    public void readInfo(ClassReader reader) {
        this.referenceKind = reader.readUnit8();
        this.referenceIndex = reader.readUnit16();
    }

    @Override
    public int tag() {
        return this.CONSTANT_TAG_METHODHANDLE;
    }
}
