package ThreadLocal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author flowscolors
 * @date 2021-12-05 10:08
 */
public class ThreadId {
    private static final AtomicInteger nextId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>(){
        @Override
        protected Integer initialValue() {
            return nextId.getAndIncrement();
        }
    };

    public static int get(){
        return nextId.get();
    }

}
