package ThreadLocal;

/**
 * @author flowscolors
 * @date 2021-12-07 11:03
 */
public class ThreadLocalTest {
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) {
        new Thread(() -> {
            for (int i = 0;i < 100 ;i++){
                threadLocal.set(i);
                System.out.println(Thread.currentThread().getName()+"======="+threadLocal.get());
                try{
                    Thread.sleep(20);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    threadLocal.remove();
                }
            }
        },"threadlocal-1").start();

        new Thread(() -> {
            for (int i = 0;i < 100 ;i++){
                threadLocal.set(i);
                System.out.println(Thread.currentThread().getName()+"======="+threadLocal.get());
                try{
                    Thread.sleep(20);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    threadLocal.remove();
                }
            }
        },"threadlocal-2").start();
    }
}
