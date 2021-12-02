package com.flowscolors.javanetty.netty.handle;

import com.flowscolors.javanetty.bean.Response;
import com.flowscolors.javanetty.netty.http.NettyHttpRequest;

/**
 * @author flowscolors
 * @date 2021-11-04 21:11
 */
public interface IFunctionHandler<T> {
    Response<T> execute(NettyHttpRequest request);
}