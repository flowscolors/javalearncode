package com.flowscolors.springbean.controller;

import com.flowscolors.springbean.util.SpringBootBeanUtil;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BeanController {
    private final ConfigurableApplicationContext applicationContext;

    public BeanController(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @RequestMapping("/bean")
    public String HelloWorld(){
        return "hello world";
    }

    @RequestMapping("/registrybean1")
    public String registrybean1(){
        InrSer ser1 = registerBean("k8-sh00b01bean", InrSer.class, "k8-sh00b01", 1);
        System.out.println(ser1);
        return "集群名： "+ser1.cluster+" 对象号 "+ser1.toString();
    }


    @RequestMapping("/getbean1")
    public String getbean1(){

        InrSer ser1 = (InrSer) SpringBootBeanUtil.getBean("k8-sh00b01bean");
        System.out.println(ser1);
        return "集群名： "+ser1.cluster+" 对象号 "+ser1.toString();
    }

    @RequestMapping("/registrybean2")
    public String registrybean2(){
        InrSer ser1 = registerBean("k8-sh00b02bean", InrSer.class, "k8-sh00b02", 2);
        System.out.println(ser1);
        return "集群名： "+ser1.cluster+" 对象号 "+ser1.toString();
    }


    @RequestMapping("/getbean2")
    public String getbean2(){

        InrSer ser1 = (InrSer) SpringBootBeanUtil.getBean("k8-sh00b02bean");
        System.out.println(ser1);
        return "集群名： "+ser1.cluster+" 对象号 "+ser1.toString();
    }


    @ToString
    public static class InrSer {
        private String cluster;
        private Integer uid;
        private String client;

        public InrSer() {
        }

        public InrSer(String cluster, Integer uid) {
            this.cluster = cluster;
            this.uid = uid;
        }
    }


    private <T> T registerBean(String name, Class<T> clazz, Object... args) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        if (args.length > 0) {
            for (Object arg : args) {
                beanDefinitionBuilder.addConstructorArgValue(arg);
            }
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }
}
