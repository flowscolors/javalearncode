package jvm.rtda.heap.methodarea;

import jvm.rtda.heap.methodarea.Object;
import jvm.rtda.heap.ClassLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flowscolors
 * @date 2021-11-12 18:18
 */
public class StringPool {

    private static Map<String, Object> internedStrs = new HashMap<>();

    public static Object jString(ClassLoader loader, String goStr) {
        Object internedStr = internedStrs.get(goStr);
        if (null != internedStr) return internedStr;

        char[] chars = goStr.toCharArray();
        Object jChars = new Object(loader.loadClass("[C"), chars);

        Object jStr = loader.loadClass("java/lang/String").newObject();
        jStr.setRefVal("value", "[C", jChars);

        internedStrs.put(goStr, jStr);
        return jStr;
    }

    public static String goString(Object jStr) {
        Object charArr = jStr.getRefVar("value", "[C");
        return new String(charArr.chars());
    }

}
