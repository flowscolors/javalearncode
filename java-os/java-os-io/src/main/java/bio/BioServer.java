package bio;

import utils.ResponseUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * @author flowscolors
 * @date 2021-12-08 1:14
 */
public class BioServer {
    private static final Integer PORT = 18080;
    private static final String IP = "127.0.0.1";

    public void start() throws IOException {
        System.out.println("开始监听端口：" + PORT);
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(IP,PORT));
        while (true) {
            //同步阻塞
            Socket socket = serverSocket.accept();
            new Thread(() -> {
                try{
                    System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
                    byte[] bytes = new byte[1024];
                    int lens = socket.getInputStream().read(bytes);
                    System.out.println("收到客户端字节数：" + lens);
                    System.out.println("收到客户端信息：" + new String(bytes, 0, lens, StandardCharsets.UTF_8));
                    
                    String serverResponse = ResponseUtil.getHttpHeader200("hello bio server");
                    socket.getOutputStream().write(serverResponse.getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                    socket.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void main(String[] args) throws IOException {
        BioServer bioServer = new BioServer();
        bioServer.start();
    }
}
