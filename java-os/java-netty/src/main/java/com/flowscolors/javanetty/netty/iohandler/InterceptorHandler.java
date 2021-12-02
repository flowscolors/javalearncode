package com.flowscolors.javanetty.netty.iohandler;


import com.flowscolors.javanetty.netty.http.NettyHttpResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.springframework.stereotype.Component;

/**
 * @author flowscolors
 * @date 2021-11-04 21:10
 * 在这里可以做拦截器，验证一些请求的合法性
 */

@ChannelHandler.Sharable
@Component
public class InterceptorHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext context, Object msg)   {
        if (isPassed((FullHttpRequest) msg)){
            context.fireChannelRead(msg);
            return;
        }

        ReferenceCountUtil.release(msg);
        context.writeAndFlush(NettyHttpResponse.make(HttpResponseStatus.UNAUTHORIZED)).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 修改实现来验证合法性
     * @param request
     * @return
     */
    private boolean isPassed(FullHttpRequest request){
        return true;
    }
}