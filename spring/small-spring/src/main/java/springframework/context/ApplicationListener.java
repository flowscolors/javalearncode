package springframework.context;

import java.util.EventListener;

/**
 * @author flowscolors
 * @date 2021-11-13 21:06
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

    /**
     * Handle an application event.
     * @param event the event to respond to
     */
    void onApplicationEvent(E event);

}