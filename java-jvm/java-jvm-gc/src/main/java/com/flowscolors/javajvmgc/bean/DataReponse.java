package com.flowscolors.javajvmgc.bean;

import lombok.Data;

@Data
public class DataReponse {
    public String causeType ;
    public Integer causeTime ;
    public StackTraceElement[] dataStackTrace;
}
