package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author flowscolors
 * @date 2021-12-08 1:37
 */
public class NioClient {

    private static final String IP = "127.0.0.1";
    private static final Integer PORT = 8888;
    ByteBuffer buffer = ByteBuffer.allocate(1024);

    public void start() {
        InetSocketAddress address = new InetSocketAddress(IP,PORT);
        try(SocketChannel socketChannel = SocketChannel.open()){
            socketChannel.connect(address);
            String toServer = "hello this is nio client";
            buffer.put(toServer.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();

            //读取服务器返回的数据
            int len = socketChannel.read(buffer);
            if (len == -1){
                return;
            }
            //重置buffer游标
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            //输出
            System.out.println("Client receive " + new String(bytes, 0, len, StandardCharsets.UTF_8));
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        NioClient nioClient= new NioClient();
        nioClient.start();
    }
}
