package code;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2022-02-21 12:08
 */
public class MultiThreadAdd {
    private  static volatile int  result = 0 ;
    //private  static int  result = 0 ;

    public static void main(String[] args) throws InterruptedException {
        int counts = 10;
        CountDownLatch countDownLatch = new CountDownLatch(10);

        for(int i = 0;i<counts;i++){
            Thread thread = new Thread(() -> {
               for (int j = 0;j<10;j++){
                   result = result + j;
               }
                countDownLatch.countDown();
            });
            thread.start();
        }

        countDownLatch.await();
        System.out.println("result = "+result);
    }
}
