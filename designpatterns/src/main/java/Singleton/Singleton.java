package Singleton;

/**
 * @author flowscolors
 * @date 2021-11-03 16:00
 */

public class Singleton {
    private static volatile Singleton singleton;

    private Singleton(){
        //添加初始化业务逻辑
    }

    //double check的单例模式，注意第二次check 与 volatile的实例。
    public static Singleton getInstance() {
        if (singleton==null){
            synchronized (Singleton.class){
                if(singleton==null){
                    singleton = new Singleton();
                }
            }
        }

        return singleton;
    }
}
