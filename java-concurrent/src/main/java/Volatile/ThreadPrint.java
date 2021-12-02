package Volatile;

/**
 * @author flowscolors
 * @date 2021-10-05 14:58
 */
public class ThreadPrint {
    private static volatile int flag = 1;

    private void printi(int i){
        while (true){
            while(flag==i){
                System.out.println(i);
                if(i==1){
                    flag = 1;
                }else if(i==2){
                    flag = 2;
                }else {
                    flag = 3;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadPrint threadPrint = new ThreadPrint();
        new Thread(() -> threadPrint.printi(1)).start();
        new Thread(() -> threadPrint.printi(2)).start();
        new Thread(() -> threadPrint.printi(3)).start();
    }
}
