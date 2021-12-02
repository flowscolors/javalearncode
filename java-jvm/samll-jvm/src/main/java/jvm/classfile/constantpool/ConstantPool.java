package jvm.classfile.constantpool;

import jvm.classfile.ClassReader;
import jvm.classfile.constantpool.impl.ConstantClassInfo;
import jvm.classfile.constantpool.impl.ConstantNameAndTypeInfo;
import jvm.classfile.constantpool.impl.ConstantUtf8Info;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-11-10 10:54
 */
public class ConstantPool {

    private ConstantInfo[] constantInfos;
    private final int size;

    //初始化常量池时，则获取常量池大小，并对data输入进行处理，对不同种类的ConstantInfo进行入队处理
    public ConstantPool(ClassReader reader) {
        this.size = reader.readUnit16();
        this.constantInfos = new ConstantInfo[size];
        for(int i=1;i < size;i++){
            constantInfos[i] = ConstantInfo.readConstantInfo(reader, this);

            switch (constantInfos[i].tag()) {
                case ConstantInfo.CONSTANT_TAG_DOUBLE:
                case ConstantInfo.CONSTANT_TAG_LONG:
                    i++;
                    break;
            }
        }
    }

    //对三种不同具体类型的constantInfo 进行处理，直接获取，从队列中返回
    public Map<String, String> getNameAndType(int idx) {
        ConstantNameAndTypeInfo constantInfo = (ConstantNameAndTypeInfo) this.constantInfos[idx];
        Map<String, String> map = new HashMap<>();
        map.put("name", this.getUTF8(constantInfo.nameIdx));
        map.put("_type", this.getUTF8(constantInfo.descIdx));
        return map;
    }

    public String getClassName(int idx){
        ConstantClassInfo classInfo = (ConstantClassInfo) this.constantInfos[idx];
        return this.getUTF8(classInfo.nameIdx);
    }

    public String getUTF8(int idx) {
        ConstantUtf8Info utf8Info = (ConstantUtf8Info) this.constantInfos[idx];
        return utf8Info == null ? "" : utf8Info.str();
    }

    //默认的get set方法
    public ConstantInfo[] getConstantInfos() {
        return constantInfos;
    }

    public void setConstantInfos(ConstantInfo[] constantInfos) {
        this.constantInfos = constantInfos;
    }

    public int getSize() {
        return size;
    }
}
