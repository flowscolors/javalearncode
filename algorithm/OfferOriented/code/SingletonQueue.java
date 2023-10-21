package code;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author flowscolors
 * @date 2022-02-20 21:08
 */
public class SingletonQueue {
    public static volatile SingletonQueue singletonQueue = null ;

    public static LinkedList realQueue = new LinkedList();

    public static Semaphore lock1 = new Semaphore(1);

    public static Semaphore lock2 = new Semaphore(0);


    private SingletonQueue () {

    }

    public static SingletonQueue getInstance() {
        if(singletonQueue == null) {
            synchronized(SingletonQueue.class){
                if(singletonQueue == null){
                    singletonQueue = new SingletonQueue() ;
                }
            }
        }
        return  singletonQueue;
    }

    public void offer(Object input){
        realQueue.add(input);
    }

    public Object poll() {
        return realQueue.remove(0);
    }

    public int size() {
        if(realQueue!=null){
            return realQueue.size();
        }
        return 1;
    }

    public static void main (String[] args) throws InterruptedException {
       SingletonQueue singletonQueue = SingletonQueue.getInstance();
        String a = "hloaiaa";
        String b = "el,lbb";

        Thread threadA = new Thread(() -> {
            try {
                for(int i = 0;i<a.length();i++) {
                    lock1.acquire();
                    singletonQueue.offer(a.charAt(i));
                    lock2.release();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                    e.printStackTrace();
            } finally{

            }

        });

        Thread threadB = new Thread(() -> {
            try{
                for(int i = 0;i<b.length();i++) {
                    lock2.acquire();
                    singletonQueue.offer(b.charAt(i));
                    lock1.release();
                    Thread.sleep(1000);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }finally{

            }


        });


        Thread threadC = new Thread(() -> {
            try {
                Thread.sleep(100);
                while(SingletonQueue.getInstance().size() > 0){
                    System.out.println(singletonQueue.poll().toString());
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });


        threadA.start();
        threadB.start();
        threadC.start();


    }

}
