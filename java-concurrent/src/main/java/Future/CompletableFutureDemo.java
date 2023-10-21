package Future;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author flowscolors
 * @date 2021-12-13 13:30
 */
public class CompletableFutureDemo {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("============ Test Combine =============");
        String result = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "hello";
        }).thenCombine(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "world";
        }), (s1, s2) -> s1 + " " + s2).join();
        System.out.println(result);

        System.out.println("============ Test Complete ============");
        CompletableFuture<String> txManagerServerCompletableFuture = CompletableFuture.supplyAsync(() -> {
            int i = 1 / 0;
            return "12";
        }).exceptionally(e -> {
            e.printStackTrace();
            return "ee";
        }).whenComplete((v, e) -> {
            System.out.println("vvvvvvvvvvvv + " + v);
            System.out.println("eeeeeeeeeeee + " + e);
        });
        String str = txManagerServerCompletableFuture.join();
        System.out.println("result: " + str);


        System.out.println("============ Test Handle  ============");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String f = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前时间为：" + df.format(new Date()));
            return "normal";
//            throw new ArithmeticException("illegal exception!");
        }).handleAsync((v, e) -> "value is: " + v + " && exception is: " + e).join();
        System.out.println(f);


        System.out.println("============ Test Compose  ============");
        int f1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "first";
        }).thenCompose(str1 -> CompletableFuture.supplyAsync(() -> {
            String str2 = "second";
            return str1.length() + str2.length();
        })).join();
        System.out.println("字符串长度为：" + f1);


        System.out.println("============ Test Run  ============");
        CompletableFuture.supplyAsync(() -> {
            System.out.println("执行CompletableFuture");
            return "first";
        }).thenRun(() -> System.out.println("finished")).join();


        System.out.println("============ Test Future  ============");
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<Integer> f4 = es.submit(() -> {
            // 长时间的异步计算
            Thread.sleep(2000L);
            System.out.println("长时间的异步计算");
            return 100;
        });
        while (true) {
            System.out.println("阻断");
            if (f4.isDone()) {

                try {
                    System.out.println(f4.get());
                    es.shutdown();
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(100L);
        }
    }
}
