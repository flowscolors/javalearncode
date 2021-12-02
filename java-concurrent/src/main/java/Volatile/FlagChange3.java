package Volatile;

/**
 * @author flowscolors
 * @date 2021-10-05 15:04
 */
public class FlagChange3 {
    private static volatile boolean flag = true;

    public void refresh(){
        flag = false;
        System.out.println("已经将flag改成false了");
    }

    public void load(){
        int i = 0;
        while (flag){
            i++;
        }
        System.out.println("跳出了循环 i="+i);
    }

    public static void main(String[] args) throws InterruptedException {
        FlagChange3 volatileTest = new FlagChange3();
        new Thread(volatileTest::load,"threadA").start();

        Thread.sleep(2000);
        new Thread(volatileTest::refresh,"threadB").start();
    }
}
