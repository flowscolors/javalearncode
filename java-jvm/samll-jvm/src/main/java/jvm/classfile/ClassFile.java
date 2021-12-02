package jvm.classfile;

import jvm.classfile.attributes.AttributeInfo;
import jvm.classfile.attributes.impl.SourceFileAttribute;
import jvm.classfile.constantpool.ConstantPool;

/**
 * @author flowscolors
 * @date 2021-11-10 9:39
 */
public class ClassFile {

    private int minorVersion;
    private int majorVersion;
    private ConstantPool constantPool;
    private int accessFlags;
    private int thisClassIdx;
    private int supperClassIdx;
    private int[] interfaces;
    private MemberInfo[] fields;
    private MemberInfo[] methods;
    private AttributeInfo[] attributes;

    //构造函数
    public ClassFile(byte[] classData) {
        ClassReader reader = new ClassReader(classData);
        this.readAndCheckMagic(reader);
        this.readAndCheckVersion(reader);
        this.constantPool = this.readConstantPool(reader);
        this.accessFlags = reader.readUnit16();
        this.thisClassIdx = reader.readUnit16();
        this.supperClassIdx = reader.readUnit16();
        this.interfaces = reader.readUnit16s();
        this.fields = MemberInfo.readMembers(reader, constantPool);
        this.methods = MemberInfo.readMembers(reader, constantPool);
        this.attributes = AttributeInfo.readAttributes(reader, constantPool);
    }

    //检查魔数 版本号
    private void readAndCheckMagic(ClassReader reader) {
        long magic = reader.readUnit32();
        if(magic != (0xCAFEBABE & 0x0FFFFFFFFL)){
            throw new ClassFormatError("magic !!! ");
        }
    }

    private void readAndCheckVersion(ClassReader reader) {
        this.minorVersion = reader.readUnit16();
        this.majorVersion = reader.readUnit16();
        switch (this.majorVersion) {
            case 45:
                return;
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
                if (this.minorVersion == 0) {
                    return;
                }
        }
        throw new UnsupportedClassVersionError();
    }

    private ConstantPool readConstantPool(ClassReader reader) {
        return new ConstantPool(reader);
    }

    public int minorVersion() {
        return this.minorVersion;
    }

    public int majorVersion() {
        return this.majorVersion;
    }

    public ConstantPool constantPool() {
        return this.constantPool;
    }

    public int accessFlags() {
        return this.accessFlags;
    }

    public MemberInfo[] fields() {
        return this.fields;
    }

    public MemberInfo[] methods() {
        return this.methods;
    }

    public String className() {
        return this.constantPool.getClassName(this.thisClassIdx);
    }

    public String superClassName() {
        if (this.supperClassIdx <= 0) {
            return "";
        }
        return this.constantPool.getClassName(this.supperClassIdx);
    }

    public String[] interfaceNames() {
        String[] interfaceNames = new String[this.interfaces.length];
        for (int i = 0; i < this.interfaces.length; i++) {
            interfaceNames[i] = this.constantPool.getClassName(interfaces[i]);
        }
        return interfaceNames;
    }

    public SourceFileAttribute sourceFileAttribute() {
        for (AttributeInfo arrInfo : this.attributes) {
            if (arrInfo instanceof SourceFileAttribute){
                return (SourceFileAttribute) arrInfo;
            }
        }
        return null;
    }
}
