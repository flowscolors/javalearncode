package Thread;

import java.util.concurrent.TimeUnit;

/**
 * @author flowscolors
 * @date 2021-11-08 18:39
 */
public class ThreadInDemo {

    public static void main(String[] args) {
        EventSend eventSend = new EventSend();
        Thread threadA = new Thread(() -> {
            eventSend.setMessage("1111");
            eventSend.send();
        },"threadA");
        Thread threadB = new Thread(() -> {
            eventSend.setMessage("2222");
            eventSend.send();
        },"threadB");

        threadA.start();
        threadB.start();
    }

}
