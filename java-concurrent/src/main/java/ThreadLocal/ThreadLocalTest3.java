package ThreadLocal;

/**
 * @author flowscolors
 * @date 2021-12-10 1:34
 */
//ThreadLocal基本使用。主线程和新建线程使用同样的变量，但是值却不一样。
public class ThreadLocalTest3 {
    public static void main(String[] args) {
        final ThreadLocal<Integer> local = new ThreadLocal<>();
        local.set(100);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " local: " + local.get());
            }
        });
        t.start();
        System.out.println("Main local: " + local.get());
    }
}
