package jvm.rtda.heap;

import jvm.classfile.ClassFile;
import jvm.classpath.Classpath;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.constantpool.RunTimeConstantPool;
import jvm.rtda.heap.methodarea.Field;
import jvm.rtda.heap.methodarea.Slots;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-11-12 11:16
 */
public class ClassLoader {
    private Classpath classpath;
    private Map<String, Class> classMap;

    public ClassLoader(Classpath classpath) {
        this.classpath = classpath;
        this.classMap = new HashMap<>();
    }

    public Class loadClass(String className) {
        Class clazz = classMap.get(className);
        if (null != clazz) return clazz;
        try {
            return loadNonArrayClass(className);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //加载！
    private Class loadNonArrayClass(String className) throws Exception {
        byte[] data = this.classpath.readClass(className);
        if (null == data) {
            throw new ClassNotFoundException(className);
        }
        Class clazz = defineClass(data);
        link(clazz);
        return clazz;
    }

    //链接 链接又分为
    private void link(Class clazz) {
        verify(clazz);
        prepare(clazz);
    }

    //准备 分配并初始化静态变量、常量、静态方法
    private void prepare(Class clazz) {
        calcInstanceFieldSlotIds(clazz);
        calcStaticFieldSlotIds(clazz);
        allocAndInitStaticVars(clazz);
    }

    private void allocAndInitStaticVars(Class clazz) {
        clazz.staticVars = new Slots(clazz.staticSlotCount);
        for (Field field : clazz.fields) {
            if (field.isStatic() && field.isFinal()) {
                initStaticFinalVar(clazz, field);
            }
        }
    }

    private void initStaticFinalVar(Class clazz, Field field) {
        Slots staticVars = clazz.staticVars;
        RunTimeConstantPool constantPool = clazz.runTimeConstantPool;
        int cpIdx = field.constValueIndex();
        int slotId = field.slotId();

        if (cpIdx > 0) {
            switch (field.descriptor()) {
                case "Z":
                case "B":
                case "C":
                case "S":
                case "I":
                    java.lang.Object val = constantPool.getConstants(cpIdx);
                    staticVars.setInt(slotId, (Integer) val);
                case "J":
                    staticVars.setLong(slotId, (Long) constantPool.getConstants(cpIdx));
                case "F":
                    staticVars.setFloat(slotId, (Float) constantPool.getConstants(cpIdx));
                case "D":
                    staticVars.setDouble(slotId, (Double) constantPool.getConstants(cpIdx));
                case "Ljava/lang/String;":
                    System.out.println("todo");
            }
        }

    }

    private void calcStaticFieldSlotIds(Class clazz) {
        int slotId = 0;
        for (Field field : clazz.fields) {
            if (field.isStatic()) {
                field.slotId = slotId;
                slotId++;
                if (field.isLongOrDouble()) {
                    slotId++;
                }
            }
        }
        clazz.staticSlotCount = slotId;
    }

    private void calcInstanceFieldSlotIds(Class clazz) {
        int slotId = 0;
        if (clazz.superClass != null) {
            slotId = clazz.superClass.instanceSlotCount;
        }
        for (Field field : clazz.fields) {
            if (!field.isStatic()) {
                field.slotId = slotId;
                slotId++;
                if (field.isLongOrDouble()) {
                    slotId++;
                }
            }
        }
        clazz.instanceSlotCount = slotId;
    }

    private void verify(Class clazz) {
        // 校验字节码，尚未实现
    }

    private Class defineClass(byte[] data) throws Exception {
        Class clazz = parseClass(data);
        clazz.loader = this;
        resolveSuperClass(clazz);
        resolveInterfaces(clazz);
        this.classMap.put(clazz.name, clazz);
        return clazz;
    }

    //解析接口实现
    private void resolveInterfaces(Class clazz) throws Exception {
        int interfaceCount = clazz.interfaceNames.length;
        if (interfaceCount > 0) {
            clazz.interfaces = new Class[interfaceCount];
            for (int i = 0; i < interfaceCount; i++) {
                clazz.interfaces[i] = clazz.loader.loadClass(clazz.interfaceNames[i]);
            }
        }
    }

    //解析超类实现
    private void resolveSuperClass(Class clazz) throws Exception {
        if (!clazz.name.equals("java/lang/Object")) {
            clazz.superClass = clazz.loader.loadClass(clazz.superClassName);
        }
    }

    private Class parseClass(byte[] data) {
        ClassFile classFile = new ClassFile(data);
        return new Class(classFile);
    }

}
