package com.flowscolors.javanetty.exception;

/**
 * @author flowscolors
 * @date 2021-11-04 21:15
 */
public class IllegalMethodNotAllowedException extends Exception {
    public IllegalMethodNotAllowedException() {
        super("METHOD NOT ALLOWED");
    }
}