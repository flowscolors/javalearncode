package jvm.classfile.attributes.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.attributes.AttributeInfo;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class ExceptionsAttribute implements AttributeInfo {

    private int[] exceptionIndexTable;

    @Override
    public void readInfo(ClassReader reader) {
        this.exceptionIndexTable = reader.readUnit16s();
    }

    public int[] exceptionIndexTable(){
        return this.exceptionIndexTable;
    }

}
