package com.flowscolors.javanetty.exception;

/**
 * @author flowscolors
 * @date 2021-11-04 21:16
 */
public class IllegalPathNotFoundException extends  Exception{
    public IllegalPathNotFoundException() {
        super("PATH NOT FOUND");
    }
}
