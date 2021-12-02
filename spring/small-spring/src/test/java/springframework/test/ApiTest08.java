package springframework.test;

import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;
import springframework.context.support.ClassPathXmlApplicationContext;
import springframework.test.bean.IUserService;

/**
 * @author flowscolors
 * @date 2021-11-13 20:44
 */
public class ApiTest08 {

    @Test
    public void test_prototype() {
        // 1.初始化 BeanFactory
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring3.xml");
        applicationContext.registerShutdownHook();

        // 2. 获取Bean对象调用方法
        IUserService userService01 = applicationContext.getBean("userService", IUserService.class);
        IUserService userService02 = applicationContext.getBean("userService", IUserService.class);

        // 3. 配置 scope="prototype/singleton"
        System.out.println("userService01 ： " + userService01);
        System.out.println("userService02 ： " + userService02);

        // 4. 打印十六进制哈希
        System.out.println(userService01 + " 十六进制哈希：" + Integer.toHexString(userService01.hashCode()));
        System.out.println(ClassLayout.parseInstance(userService01).toPrintable());
    }

    @Test
    public void test_factory_bean() {
        // 1.初始化 BeanFactory
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring3.xml");
        applicationContext.registerShutdownHook();
        // 2. 调用代理方法
        IUserService userService = applicationContext.getBean("userService", IUserService.class);
        System.out.println("测试结果：" + userService.queryUserInfo());
    }

}
