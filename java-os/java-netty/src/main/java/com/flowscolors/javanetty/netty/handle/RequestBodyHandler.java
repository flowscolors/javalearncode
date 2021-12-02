package com.flowscolors.javanetty.netty.handle;

import com.flowscolors.javanetty.bean.Response;
import com.flowscolors.javanetty.netty.annotation.NettyHttpHandler;
import com.flowscolors.javanetty.netty.http.NettyHttpRequest;

/**
 * @author flowscolors
 * @date 2021-11-04 21:13
 */
@NettyHttpHandler(path = "/request/body",method = "POST")
public class RequestBodyHandler implements IFunctionHandler<String> {
    @Override
    public Response<String> execute(NettyHttpRequest request) {
        /**
         * 可以在此拿到json转成业务需要的对象
         */
        String json = request.contentText();
        return Response.ok(json);
    }
}
