package jvm.rtda.heap.constantpool;

import jvm.classfile.constantpool.impl.ConstantInterfaceMethodRefInfo;
import jvm.rtda.heap.methodarea.Method;
import jvm.rtda.heap.methodarea.MethodLookup;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.heap.methodarea.Object;

public class InterfaceMethodRef extends MemberRef {

    private Method method;

    public static InterfaceMethodRef newInterfaceMethodRef(RunTimeConstantPool runTimeConstantPool, ConstantInterfaceMethodRefInfo refInfo) {
        InterfaceMethodRef ref = new InterfaceMethodRef();
        ref.runTimeConstantPool = runTimeConstantPool;
        ref.copyMemberRefInfo(refInfo);
        return ref;
    }

    public Method resolvedInterfaceMethod() {
        if (this.method == null) {
            this.resolveInterfaceMethodRef();
        }
        return this.method;
    }

    private void resolveInterfaceMethodRef() {
        Class d = this.runTimeConstantPool.getClazz();
        Class c = this.resolvedClass();
        if (!c.isInterface()) {
            throw new IncompatibleClassChangeError();
        }

        Method method = lookupInterfaceMethod(c, this.name, this.descriptor);
        if (null == method) {
            throw new NoSuchMethodError();
        }

        if (!method.isAccessibleTo(d)){
            throw new IllegalAccessError();
        }

        this.method = method;
    }

    private Method lookupInterfaceMethod(Class iface, String name, String descriptor) {
        for (Method method : iface.methods) {
            if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                return method;
            }
        }
        return MethodLookup.lookupMethodInInterfaces(iface.interfaces, name, descriptor);
    }


}
