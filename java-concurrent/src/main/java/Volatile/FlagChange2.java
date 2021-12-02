package Volatile;

import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2021-10-05 15:03
 */
public class FlagChange2 {
    private static boolean flag = false;
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
            new Why(10);
        }
        System.out.println("程序结束" );
    }
}

class  Why {
    private int value;
    //private final  int value;
    Why(int value){
        this.value = value;
    }

}

