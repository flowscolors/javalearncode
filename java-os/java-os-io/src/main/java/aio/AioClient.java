package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author flowscolors
 * @date 2021-11-20 20:46
 */
public class AioClient {
    private static final Integer PORT = 8888;
    private static final String IP = "127.0.0.1";
    private static final Integer TIMEOUT = 5;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private ByteBuffer byteBuffer;
    private AsynchronousSocketChannel asynchronousSocketChannel;

    public AioClient() {
        try{
            //1、 打开一个SocketChannel通道并获取AsynchronousSocketChannel实例
            asynchronousSocketChannel = AsynchronousSocketChannel.open();
        }catch (IOException e){
            e.printStackTrace();
        }
        byteBuffer = ByteBuffer.allocate(1024);
    }

    public void start() {
        //2、 实现一个CompletionHandler回调接口handler，
        CompletionHandler<Void,Object> handler = new CompletionHandler<Void, Object>() {
            @Override
            public void completed(Void result, Object attachment) {
                try{
                    byteBuffer.clear();
                    String toServer = "hello this is client";
                    byteBuffer.put(toServer.getBytes(StandardCharsets.UTF_8));
                    byteBuffer.flip();
                    asynchronousSocketChannel.write(byteBuffer).get();
                    byteBuffer.clear();
                    Integer receivedCount =  asynchronousSocketChannel.read(byteBuffer).get(TIMEOUT,TIME_UNIT);
                    System.out.println("收到服务端信息：" + new String(byteBuffer.array(), 0, receivedCount, StandardCharsets.UTF_8));
                } catch (InterruptedException | ExecutionException | TimeoutException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("failed : " + exc.getMessage());
            }
        };
        //3、 连接到服务器并处理连接结果
        asynchronousSocketChannel.connect(new InetSocketAddress(IP,PORT),null,handler);
    }

    public static void main(String[] args) throws InterruptedException {
        AioClient aioClient = new AioClient();
        aioClient.start();
        Thread.currentThread().join();
    }
}
