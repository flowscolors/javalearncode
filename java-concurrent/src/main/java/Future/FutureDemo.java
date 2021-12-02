package Future;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author flowscolors
 * @date 2021-11-03 11:57
 */
public class FutureDemo {
    public static void main(String[] args) {

        //创建线程池

        ExecutorService service = Executors.newFixedThreadPool(10);

        //提交任务，并用 Future 接收返回结果

        ArrayList<Future> allFutures = new ArrayList<>();

        for (int i = 0; i < 4; i++) {

            Future<String> future;

            if (i == 0 || i == 1) {

                future = service.submit(new SlowTask());

            } else {

                future = service.submit(new FastTask());

            }

            allFutures.add(future);

        }

        for (int i = 0; i < 4; i++) {

            Future<String> future = allFutures.get(i);

            try {

                String result = future.get();

                System.out.println(result);

            } catch (InterruptedException e) {

                e.printStackTrace();

            } catch (ExecutionException e) {

                e.printStackTrace();

            }

        }

        service.shutdown();

    }

    static class SlowTask implements Callable<String> {

        @Override

        public String call() throws Exception {

            Thread.sleep(5000);

            return "速度慢的任务";

        }

    }

    static class FastTask implements Callable<String> {

        @Override

        public String call() throws Exception {

            return "速度快的任务";

        }

    }

}
