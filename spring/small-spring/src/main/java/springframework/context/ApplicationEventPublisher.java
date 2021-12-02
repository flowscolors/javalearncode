package springframework.context;

import springframework.context.ApplicationEvent;

/**
 * @author flowscolors
 * @date 2021-11-13 21:05
 */
//事件发布者接口
public interface ApplicationEventPublisher {

    /**
     * Notify all listeners registered with this application of an application
     * event. Events may be framework events (such as RequestHandledEvent)
     * or application-specific events.
     * @param event the event to publish
     */
    void publishEvent(ApplicationEvent event);

}