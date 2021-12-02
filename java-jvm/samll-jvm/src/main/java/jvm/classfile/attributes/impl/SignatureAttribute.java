package jvm.classfile.attributes.impl;

import jvm.classfile.ClassReader;
import jvm.classfile.attributes.AttributeInfo;
import jvm.classfile.constantpool.ConstantPool;

/**
 * http://www.itstack.org
 * create by fuzhengwei on 2019/4/26
 */
public class SignatureAttribute implements AttributeInfo {

    private ConstantPool constantPool;
    private int signatureIdx;

    public SignatureAttribute(ConstantPool constantPool) {
          this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        this.signatureIdx = reader.readUnit16();
    }

    public String signature(){
        return this.constantPool.getUTF8(this.signatureIdx);
    }

}
