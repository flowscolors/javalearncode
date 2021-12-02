package springframework.beans;

/**
 * @author flowscolors
 * @date 2021-11-07 22:19
 */
public class BeansException extends RuntimeException{

    public BeansException(String msg){
        super(msg);
    }

    public BeansException(String msg,Throwable cause){
        super(msg,cause);
    }
}
