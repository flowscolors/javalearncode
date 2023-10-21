package ThreadLocal;

/**
 * @author flowscolors
 * @date 2021-12-07 11:14
 */
public class ThreadLocalTest2 {
    static class TestClass {
        private ThreadLocal<Integer> i = new ThreadLocal<Integer>(){
            Integer integer;

            @Override
            protected Integer initialValue() {
                return 0;
            }
        };

        public Integer get() {
            return i.get();
        }

        public void set(Integer integer){
            i.set(i.get()+integer);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TestClass testClass = new TestClass();
        for(int i = 0 ;i < 100;i++){
            new Thread(() -> {
                for(int j = 1;j<100;j++){
                    testClass.set(j);
                }
                System.out.println("testClass.get() = "+testClass.get());
            }
            ).start();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }
}
