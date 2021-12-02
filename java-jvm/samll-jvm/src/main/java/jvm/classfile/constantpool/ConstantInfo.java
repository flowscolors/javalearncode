package jvm.classfile.constantpool;

import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.impl.*;

/**
 * @author flowscolors
 * @date 2021-11-10 10:54
 */
public interface ConstantInfo {

    //这些tag就是JVM中常量标志位对应的tag
    int CONSTANT_TAG_CLASS = 7;
    int CONSTANT_TAG_FIELDREF = 9;
    int CONSTANT_TAG_METHODREF = 10;
    int CONSTANT_TAG_INTERFACEMETHODREF = 11;
    int CONSTANT_TAG_STRING = 8;
    int CONSTANT_TAG_INTEGER = 3;
    int CONSTANT_TAG_FLOAT = 4;
    int CONSTANT_TAG_LONG = 5;
    int CONSTANT_TAG_DOUBLE = 6;
    int CONSTANT_TAG_NAMEANDTYPE = 12;
    int CONSTANT_TAG_UTF8 = 1;
    int CONSTANT_TAG_METHODHANDLE = 15;
    int CONSTANT_TAG_METHODTYPE = 16;
    int CONSTANT_TAG_INVOKEDYNAMIC = 18;

    void readInfo(ClassReader reader);

    int tag();

    //输入data，每次获取标志位，并根据标志位，进行常量初始化处理
    static ConstantInfo readConstantInfo(ClassReader reader, ConstantPool constantPool) {
        int tag = reader.readUnit8();
        //System.out.println("tag : " + tag);
        ConstantInfo constantInfo = newConstantInfo(tag, constantPool);
        constantInfo.readInfo(reader);
        return constantInfo;
    }

    //根据标志位进行相关处理,大致分三类，基本数据类型、String、Classs对象类型、方法 方法Handler 反射类型
    static ConstantInfo newConstantInfo(int tag, ConstantPool constantPool) {
        switch (tag){
            case CONSTANT_TAG_INTEGER:
                return new ConstantIntegerInfo();
            case CONSTANT_TAG_FLOAT:
                return new ConstantFloatInfo();
            case CONSTANT_TAG_LONG:
                return new ConstantLongInfo();
            case CONSTANT_TAG_DOUBLE:
                return new ConstantDoubleInfo();
            case CONSTANT_TAG_UTF8:
                return new ConstantUtf8Info();
            case CONSTANT_TAG_STRING:
                return new ConstantStringInfo(constantPool);
            case CONSTANT_TAG_CLASS:
                return new ConstantClassInfo(constantPool);
            case CONSTANT_TAG_FIELDREF:
                return new ConstantFieldRefInfo(constantPool);
            case CONSTANT_TAG_METHODREF:
                return new ConstantMethodRefInfo(constantPool);
            case CONSTANT_TAG_INTERFACEMETHODREF:
                return new ConstantInterfaceMethodRefInfo(constantPool);
            case CONSTANT_TAG_NAMEANDTYPE:
                return new ConstantNameAndTypeInfo();
            case CONSTANT_TAG_METHODTYPE:
                return new ConstantMethodTypeInfo();
            case CONSTANT_TAG_METHODHANDLE:
                return new ConstantMethodHandleInfo();
            case CONSTANT_TAG_INVOKEDYNAMIC:
                return new ConstantInvokeDynamicInfo();
            default:
                throw new ClassFormatError("constant pool tag");
        }


    }
}
