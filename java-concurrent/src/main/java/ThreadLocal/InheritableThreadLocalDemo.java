package ThreadLocal;

import lombok.extern.slf4j.Slf4j;

/**
 * @author flowscolors
 * @date 2021-10-04 19:44
 */
@Slf4j
public class InheritableThreadLocalDemo {
    private static InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

    public static void main(String[] args) {
        inheritableThreadLocal.set("A");

        // 在创建线程的时候, 将 InheritableThreadLocal 中的值拷贝一份到线程中
        Thread thread = new Thread(() -> {
            log.info("value = {}", inheritableThreadLocal.get());
            // 在子线程中将值设置为 B
            inheritableThreadLocal.set("B");
        });

        // 先 start 再 join, 让 main 线程等待子线程执行完成
        thread.start();
        try {
            // 等待线程执行完成
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 子线程中虽然能够获取到 main 线程中设置的值
        // 但是在子线程中设置 InheritableThreadLocal 的值并不会影响 main 线程中 InheritableThreadLocal 的值
        log.info("value = {}", inheritableThreadLocal.get());
    }
}
