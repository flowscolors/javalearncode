package jvm.rtda.heap.methodarea;

/**
 * @author flowscolors
 * @date 2021-11-12 18:17
 */
public class ClassHierarchy {
    private Class clazz;

    public static ClassHierarchy util(Class clazz) {
        ClassHierarchy classHierarchy = new ClassHierarchy();
        classHierarchy.clazz = clazz;
        return classHierarchy;
    }

    public boolean isAssignableFrom(Class other) {
        Class s = this.clazz;
        Class t = other;

        if (s == t) {
            return true;
        }

        if (!s.isArray()) {
            if (!s.isInterface()) {
                if (!t.isInterface()) {
                    return s.isSubClassOf(t);
                } else {
                    return isImplements(t);
                }
            } else {
                if (!t.isInterface()) {
                    return t.isJlObject();
                } else {
                    return t.isSubInterfaceOf(s);
                }
            }
        } else {
            if (!t.isArray()) {

                if (!t.isInterface()) {
                    return t.isJlObject();
                } else {
                    return t.isJlCloneable() || t.isJioSerializable();
                }
            } else {
                Class sc = s.componentClass();
                Class tc = t.componentClass();
                return sc == tc || tc.isAssignableFrom(sc);
            }
        }
    }

    public boolean isImplements(Class iface) {
        for (Class c = this.clazz; c != null; c = c.superClass) {
            for (Class i : c.interfaces) {
                if (i == iface || i.isSubInterfaceOf(iface)) {
                    return true;
                }
            }
        }
        return false;
    }

}
