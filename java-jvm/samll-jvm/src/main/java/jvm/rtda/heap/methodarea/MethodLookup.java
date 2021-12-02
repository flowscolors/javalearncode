package jvm.rtda.heap.methodarea;

/**
 * @author flowscolors
 * @date 2021-11-12 14:56
 */
public class MethodLookup {

    //如果一个类是另一类的子类，那么父类的方法也全部要加载进来
    static public Method lookupMethodInClass(Class clazz, String name, String descriptor) {
        for (Class c = clazz; c != null; c = c.superClass) {
            for (Method method : c.methods) {
                if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                    return method;
                }
            }
        }
        return null;
    }

    static public Method lookupMethodInInterfaces(Class[] ifaces, String name, String descriptor) {
        for (Class inface : ifaces) {
            for (Method method : inface.methods) {
                if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                    return method;
                }
            }
        }
        return null;
    }


}
