package Synchronized;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author flowscolors
 * @date 2022-02-13 11:20
 */
public class jol_object {
    public static void main(String[] args) {
        Object object = new Object();
        System.out.println(ClassLayout.parseInstance(object).toPrintable());

        Object[] objects = new Object[100];
        System.out.println(ClassLayout.parseInstance(objects).toPrintable());
    }
}
