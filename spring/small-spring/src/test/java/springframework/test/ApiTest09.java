package springframework.test;

import org.junit.Test;
import springframework.context.support.ClassPathXmlApplicationContext;
import springframework.test.event.CustomEvent;

/**
 * @author flowscolors
 * @date 2021-11-13 21:17
 */
public class ApiTest09 {

    @Test
    public void test_event() {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring4.xml");
        applicationContext.publishEvent(new CustomEvent(applicationContext, 1019129009086763L, "成功了！"));

        applicationContext.registerShutdownHook();
    }
}
