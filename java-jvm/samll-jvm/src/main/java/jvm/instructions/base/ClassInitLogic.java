package jvm.instructions.base;

import jvm.rtda.Frame;
import jvm.rtda.heap.methodarea.Method;
import jvm.rtda.heap.methodarea.Class;
import jvm.rtda.Thread;

/**
 * @author flowscolors
 * @date 2021-11-12 15:02
 */
public class ClassInitLogic {
    public static void initClass(Thread thread, Class clazz) {
        clazz.startInit();
        scheduleClinit(thread, clazz);
        initSuperClass(thread, clazz);
    }

    private static void scheduleClinit(Thread thread, Class clazz) {
        Method clinit = clazz.getClinitMethod();
        if (null == clinit) return;
        Frame newFrame = thread.newFrame(clinit);
        thread.pushFrame(newFrame);
    }

    private static void initSuperClass(Thread thread, Class clazz) {
        if (clazz.isInterface()) return;
        Class superClass = clazz.superClass();
        if (null != superClass && !superClass.initStarted()) {
            initClass(thread, superClass);
        }
    }
}
