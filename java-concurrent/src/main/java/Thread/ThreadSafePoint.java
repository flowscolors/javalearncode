package Thread;

import lombok.extern.slf4j.Slf4j;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author flowscolors
 * @date 2021-12-09 12:31
 */
@Slf4j
public class ThreadSafePoint {
    public static AtomicInteger num = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
          for(int i = 0;i < 200000000;i++){
              num.getAndAdd(1);
          }
        };
        Thread thread1 = new Thread(runnable);
        Thread thread2 = new Thread(runnable);
        thread1.start();
        thread2.start();
        log.info("主线程sleep");
        Thread.sleep(1000);
        log.info("主线程唤醒 num = " + num);
        //通常情况下主线程的输出会按主线程的逻辑来，而另外两个线程异步的走，而在该例中主线程一直等待另外线程执行结束。
    }
}
