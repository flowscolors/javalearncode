package springframework.test;

import org.junit.Test;
import springframework.beans.PropertyValue;
import springframework.beans.PropertyValues;
import springframework.beans.factory.config.BeanDefinition;

import springframework.beans.factory.config.BeanReference;
import springframework.beans.factory.support.DefaultListableBeanFactory;
import springframework.test.bean.UserDao;
import springframework.test.bean.UserService;

/**
 * @author flowscolors
 * @date 2021-11-07 9:59
 */
public class ApiTest {
    @Test
    public void test_BeanFactory(){

        // 1.初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2.注入bean
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class);
        beanFactory.registerBeanDefinition("userService",beanDefinition);

        // 3.获取bean
        //注意只要带了构造方法 就会触发这个，这里看到的是没传args，实际也是要类里写了构造方法，也即对类有要求了
        //如果类中无构造方法，则会触发Superclass has no null constructors but no arguments were given的报错。
        //并且这里creatBean底层是使用的是单例模式，于是后续的getBean哪怕传了初始化参数，也只会拿到第一次创建的bean。如果使用的是范围，则每次拿的是不同值，但这时候多线程就要考虑自己的处理和别人的处理了。
        UserService userService = (UserService) beanFactory.getBean("userService");
        System.out.println(userService.toString());
        userService.queryUserInfo();

        UserService userService2 = (UserService) beanFactory.getBean("userService","joker");
        System.out.println(userService2.toString());
        userService2.queryUserInfo();

        UserService userService3 = (UserService) beanFactory.getSingleton("userService");
        System.out.println(userService3.toString());
        userService3.queryUserInfo();
    }


    @Test
    public void test_BeanFactory_04() {
        // 1.初始化 BeanFactory
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        // 2. UserDao 注册
        beanFactory.registerBeanDefinition("userDao", new BeanDefinition(UserDao.class));

        // 3. UserService 设置属性[uId、userDao]
        PropertyValues propertyValues = new PropertyValues();
        propertyValues.addPropertyValue(new PropertyValue("uId", "10001"));
        propertyValues.addPropertyValue(new PropertyValue("userDao",new BeanReference("userDao")));

        // 4. UserService 注入bean
        BeanDefinition beanDefinition = new BeanDefinition(UserService.class, propertyValues);
        beanFactory.registerBeanDefinition("userService", beanDefinition);

        // 5. UserService 获取bean
        UserService userService = (UserService) beanFactory.getBean("userService");
        userService.queryUserInfo();
    }
}
