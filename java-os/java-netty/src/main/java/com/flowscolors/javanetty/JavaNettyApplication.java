package com.flowscolors.javanetty;

import com.flowscolors.javanetty.netty.annotation.NettyHttpHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(includeFilters = @ComponentScan.Filter(NettyHttpHandler.class))
public class JavaNettyApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(JavaNettyApplication.class).web(WebApplicationType.NONE).run(args);
        //SpringApplication.run(JavaNettyApplication.class, args);
    }

}
