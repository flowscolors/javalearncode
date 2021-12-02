package com.flowscolors.javajvmgc.controller;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.Unsafe;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class JvmController {

    /* VM Options:
       -Xms20m
       -Xmx20m
       -XX:MaxDirectMemorySize=10M
       -XX:PermSize=10M
       -XX:MaxPermSize=10M
       -XX:NativeMemoryTracking=summary
     */

    static class OOMObject{}
    private int stackLength = 1;
    private static final int _1MB = 1024 * 1024;

    @RequestMapping("/hello")
    public String HelloWorld()  {
        return "hello world";
    }

    //并不会导致应用异常退出的OOM 比如大量的查询存到ArrayList里面，20M存了24w条数据。但是这里OOM退出后就会因为没有GC Root被GC掉。
    @RequestMapping("/HeapOOM")
    public String HeapOOM()  {
        List<OOMObject> list = new ArrayList<OOMObject>();
        try{
            while (true){
                list.add(new OOMObject());
            }
        }catch (Throwable e){
            System.out.println("ArrayList length:" + list.size());
            e.printStackTrace();
            return "ArrayList length:" + list.size() +"<br>" +throwable2stirng(e);
        }
    }

    //会导致程序异常退出的OOM，哪怕线程异常退出，ArrayList中存放的数据也不会丢失

    //通过重复栈帧栈满栈空间的StackOverFlow 默认2M栈空间，可以存到18234左右的帧深度
    @RequestMapping("/StackOverflowError")
    public String StackOverflowError() {
        try{
            this.stackLeak();
            return "stack length:" + stackLength;
        }catch (Throwable e){
            System.out.println("stack length:" + stackLength);
            e.printStackTrace();
            return "stack length:" + stackLength +"<br>" +throwable2stirng(e);
        }
    }

    //直接内存分配错误
    @RequestMapping("/DirectMemoryOOM")
    public String DirectMemoryOOM() {
        int allocateTime = 0;
        try{
            Field unsafeField = Unsafe.class.getDeclaredFields()[0];
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            while (true) {
                allocateTime++;
                unsafe.allocateMemory(_1MB);
            }
        }catch (Throwable e){
            System.out.println("allocate Time :" + allocateTime);
            e.printStackTrace();
            return "allocate Time :" + allocateTime +"<br>" +throwable2stirng(e);
        }
    }

    //MetaSpace分配错误

    //方法区PermSize分配错误
    @RequestMapping("/MethodAreaOOM")
    public String MethodAreaOOM()  {
        int methodUseTime = 0;
        try{
            while (true) {
                methodUseTime++;
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(OOMObject.class);
                enhancer.setUseCache(false);
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                        return proxy.invokeSuper(obj, args);
                    }
                });
                enhancer.create();
            }
        }catch (Throwable e){
            System.out.println("methodUse Time :" + methodUseTime);
            e.printStackTrace();
            return "methodUse Time :" + methodUseTime +"<br>" +throwable2stirng(e);
        }
    }


    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public String throwable2stirng (Throwable e){
        Writer write = new StringWriter();
        e.printStackTrace(new PrintWriter(write));
        return  write.toString().replace(")",")<br>&nbsp;&nbsp;");
    }

}


