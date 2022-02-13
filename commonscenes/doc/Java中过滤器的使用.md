
首先拦截器和过滤器都是基于AOP实现的。而两者的主要区别包括以下几个方面：
                    
1、Filter是依赖于Servlet容器，属于Servlet规范的一部分，而拦截器则是独立存在的，可以在任何情况下使用。
                    
2、Filter的执行由Servlet容器回调完成，而拦截器通常通过动态代理的方式来执行。
                    
3、Filter的生命周期由Servlet容器管理，而拦截器则可以通过IoC容器来管理，因此可以通过注入等方式来获取其他Bean的实例，因此使用会更方便。
Tomcat  -> Filter -> Servlet -> Inteceptor -> Controller 

过滤器的实现直接 implements Filter 即可。实现类需要实现init()、doFilter()、destory()接口。关键在于doFilter()。
除了实现对应方法，还需要使用对应的Java Config文件，或者使用注解完成对应注入工作。

下面这个过滤器就能够实现记录请求执行时间的功能。当然使用拦截器也能执行相关功能。
```shell script
public class LogCostFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
 
    }
 
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long start = System.currentTimeMillis();
        filterChain.doFilter(servletRequest,servletResponse);
        System.out.println("Execute cost="+(System.currentTimeMillis()-start));
    }
 
    @Override
    public void destroy() {
 
    }
}
```

使用拦截器实现相关功能。虽然拦截器在很多场景下优于过滤器，但是在这种场景下，过滤器比拦截器实现起来更简单。
```shell script
public class LogCostInterceptor implements HandlerInterceptor {
    long start = System.currentTimeMillis();
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        start = System.currentTimeMillis();
        return true;
    }
 
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        System.out.println("Interceptor cost="+(System.currentTimeMillis()-start));
    }
 
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
```

当然由于过滤器和拦截器都是基于AOP的，所以实际使用AOP也可以实现上面的功能，甚至可以实现更多功能。

参考文档：
https://www.cnblogs.com/paddix/p/8365558.html