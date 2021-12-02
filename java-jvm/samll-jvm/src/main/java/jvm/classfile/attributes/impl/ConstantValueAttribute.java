package jvm.classfile.attributes.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.attributes.AttributeInfo;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class ConstantValueAttribute implements AttributeInfo {

    private int constantValueIdx;

    @Override
    public void readInfo(ClassReader reader) {
        this.constantValueIdx = reader.readUnit16();
    }

    public int constantValueIdx(){
        return this.constantValueIdx;
    }

}
