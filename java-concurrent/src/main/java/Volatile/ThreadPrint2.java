package Volatile;

/**
 * @author flowscolors
 * @date 2021-10-05 14:59
 */
public class ThreadPrint2 {
    private static volatile int flag = 1;

    public static void main(String[] args) throws InterruptedException {
        Thread ThreadA = new Thread(() -> {
            while (true){
                while(flag==1){
                    System.out.println("1");
                    flag = 2;
                }
            }
        });
        Thread ThreadB = new Thread(() -> {
            while (true){
                while(flag==2){
                    System.out.println("2");
                    flag = 3;
                }
            }
        });
        Thread ThreadC = new Thread(() -> {
            while (true){
                while(flag==3){
                    System.out.println("3");
                    flag = 1;
                }
            }
        });
        ThreadA.start();
        ThreadB.start();
        ThreadC.start();
    }
}
