package Singleton;

/**
 * @author flowscolors
 * @date 2021-11-03 16:00
 */
//需要使用volatile synchronized 同时判断 加两次对实例不为null的判断
public class Singleton {
    private static volatile Singleton singleton;  //一定需要volatile

    private Singleton(){
        //添加初始化业务逻辑
    }

    //double check的单例模式，注意第二次check 与 volatile的实例。
    public static Singleton getInstance() {
        if (singleton==null){                      //不为空 则直接使用。否则就初始化，但是保证只有一个线程来初始化
            synchronized (Singleton.class){
                if(singleton==null){
                    singleton = new Singleton();
                }
            }
        }

        return singleton;
    }
}
