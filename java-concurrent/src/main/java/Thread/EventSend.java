package Thread;

import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2021-11-08 18:34
 */
public class EventSend {
    public  String  message ;

    public void setMessage(String message){
        this.message = message;
    }

    public void send(){
        try{
            String msg = this.message;
            if(Thread.currentThread().getName().equals("threadA")){
                TimeUnit.SECONDS.sleep(2);
            }
            System.out.println(Thread.currentThread().getName()+"  send  "+msg);
            //System.out.println(Thread.currentThread().getName()+"  send  "+this.message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
