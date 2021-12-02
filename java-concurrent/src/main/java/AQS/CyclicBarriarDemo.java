package AQS;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author flowscolors
 * @date 2021-10-04 19:38
 */
@Slf4j
public class CyclicBarriarDemo {
    private static final int threadCount = 5;

    private static final CyclicBarrier barrier = new CyclicBarrier(threadCount, new Runnable() {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " 完成最后任务");
        }
    });

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < threadCount; i++){
            final int threadNum = i;
            exec.execute(() -> {
                try {
                    test(threadNum);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        log.info("finish");
        exec.shutdown();
    }
    private static void test(int threadNum) throws BrokenBarrierException, InterruptedException {
        Thread.sleep(1000);
        System.out.println(Thread.currentThread().getName() + " 到达栅栏 A");
        barrier.await();
        System.out.println(Thread.currentThread().getName() + " 冲破栅栏 A");

        Thread.sleep(2000);
        System.out.println(Thread.currentThread().getName() + " 到达栅栏 B");
        barrier.await();
        System.out.println(Thread.currentThread().getName()+ " 冲破栅栏 B");
        log.info("{}", threadNum);
    }
}
