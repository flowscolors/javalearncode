package com.flowscolors.javaspi.exception;

/**
 * @author flowscolors
 * @date 2021-12-13 11:21
 */
public class ObjectSerializerException extends Exception {
    private static final long serialVersionUID = -948934144333391208L;

    public ObjectSerializerException() {
    }

    public ObjectSerializerException(String message) {
        super(message);
    }

    public ObjectSerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectSerializerException(Throwable cause) {
        super(cause);
    }
}