package com.flowscolors.springcloudeureka.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path={"/test4"})
public class HelloController {

    @RequestMapping("/hello")
    public String HelloWorld(){
        return "hello world";
    }
}
