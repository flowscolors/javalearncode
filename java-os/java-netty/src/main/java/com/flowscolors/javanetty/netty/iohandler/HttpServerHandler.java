package com.flowscolors.javanetty.netty.iohandler;

import com.flowscolors.javanetty.bean.Path;
import com.flowscolors.javanetty.bean.Response;
import com.flowscolors.javanetty.exception.IllegalMethodNotAllowedException;
import com.flowscolors.javanetty.exception.IllegalPathDuplicatedException;
import com.flowscolors.javanetty.exception.IllegalPathNotFoundException;
import com.flowscolors.javanetty.netty.annotation.NettyHttpHandler;
import com.flowscolors.javanetty.netty.handle.IFunctionHandler;
import com.flowscolors.javanetty.netty.http.NettyHttpRequest;
import com.flowscolors.javanetty.netty.http.NettyHttpResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author flowscolors
 * @date 2021-11-04 21:09
 */

@ChannelHandler.Sharable
@Component
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);

    private HashMap<Path, IFunctionHandler> functionHandlerMap = new HashMap<>();

    private ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = Executors.defaultThreadFactory().newThread(runnable);
        thread.setName("NettyHttpHandler-" + thread.getName());
        return thread;
    });

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpRequest copyRequest = request.copy();
        executor.execute(() -> onReceivedRequest(ctx,new NettyHttpRequest(copyRequest)));
    }


    private void onReceivedRequest(ChannelHandlerContext context, NettyHttpRequest request){
        FullHttpResponse response = handleHttpRequest(request);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ReferenceCountUtil.release(request);
    }

    private FullHttpResponse handleHttpRequest(NettyHttpRequest request) {

        IFunctionHandler functionHandler = null;

        try {
            functionHandler = matchFunctionHandler(request);
            Response response =  functionHandler.execute(request);
            return NettyHttpResponse.ok(response.toJSONString());
        }
        catch (IllegalMethodNotAllowedException error){
            return NettyHttpResponse.make(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        catch (IllegalPathNotFoundException error){
            return NettyHttpResponse.make(HttpResponseStatus.NOT_FOUND);
        }
        catch (Exception error){
            LOGGER.error(functionHandler.getClass().getSimpleName() + " Error",error);
            return NettyHttpResponse.makeError(error);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        Map<String, Object> handlers =  applicationContext.getBeansWithAnnotation(NettyHttpHandler.class);
        for (Map.Entry<String, Object> entry : handlers.entrySet()) {
            Object handler = entry.getValue();
            Path path = Path.make(handler.getClass().getAnnotation(NettyHttpHandler.class));
            if (functionHandlerMap.containsKey(path)){
                LOGGER.error("IFunctionHandler has duplicated :" + path.toString(),new IllegalPathDuplicatedException());
                System.exit(0);
            }
            functionHandlerMap.put(path, (IFunctionHandler) handler);
        }
    }

    private IFunctionHandler matchFunctionHandler(NettyHttpRequest request) throws IllegalPathNotFoundException, IllegalMethodNotAllowedException {

        AtomicBoolean matched = new AtomicBoolean(false);

        Stream<Path> stream = functionHandlerMap.keySet().stream()
                .filter(((Predicate<Path>) path -> {
                    /**
                     *过滤 Path URI 不匹配的
                     */
                    if (request.matched(path.getUri(), path.isEqual())) {
                        matched.set(true);
                        return matched.get();
                    }
                    return false;

                }).and(path -> {
                    /**
                     * 过滤 Method 匹配的
                     */
                    return request.isAllowed(path.getMethod());
                }));

        Optional<Path> optional = stream.findFirst();

        stream.close();

        if (!optional.isPresent() && !matched.get()){
            throw  new IllegalPathNotFoundException();
        }

        if (!optional.isPresent() && matched.get()){
            throw  new IllegalMethodNotAllowedException();
        }

        return functionHandlerMap.get(optional.get());
    }

}