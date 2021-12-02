package com.flowscolors.javanetty.netty.handle;

import com.flowscolors.javanetty.bean.Response;
import com.flowscolors.javanetty.netty.annotation.NettyHttpHandler;
import com.flowscolors.javanetty.netty.http.NettyHttpRequest;

/**
 * @author flowscolors
 * @date 2021-11-04 21:13
 */
@NettyHttpHandler(path = "/hello/world")
public class HelloWorldHandler implements IFunctionHandler<String> {

    @Override
    public Response<String> execute(NettyHttpRequest request) {
        return Response.ok("Hello World");
    }
}
