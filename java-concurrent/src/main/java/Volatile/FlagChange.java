package Volatile;

import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2021-10-05 15:02
 */
public class FlagChange {
    private static boolean flag = false;
    //private static int i = 0;
    private static Integer i = 0;
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                flag = true;
                System.out.println("flag 被修改成 true");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        while (!flag) {
            i++;
        }
        System.out.println("程序结束,i=" + i);
    }
}
