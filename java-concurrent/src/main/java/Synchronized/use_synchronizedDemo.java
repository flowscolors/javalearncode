package Synchronized;

/**
 * @author flowscolors
 * @date 2021-12-09 15:58
 */
public class use_synchronizedDemo {

    private Object lockObject;

    public use_synchronizedDemo(Object lockObject){
        this.lockObject = lockObject;
    }

    public void lockObject(){
        synchronized (lockObject){   //作用于lockObject这个对象
            System.out.println("修饰代码块");
        }
    }

    public synchronized void lockInstance() {  //作用于use_synchronizedDemo这个实例
        System.out.println("修饰实例方法");
    }

    public static synchronized void lockStatic() { //作用于use_synchronizedDemo.class这个类对象
        System.out.println("修饰静态方法");
    }

    public static void main(String[] args) {
        Object object = new Object();
        use_synchronizedDemo use_synchronizedDemo = new use_synchronizedDemo(object);
    }

}
