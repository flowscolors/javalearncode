package bio;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author flowscolors
 * @date 2021-12-08 1:14
 */
public class BioClient {
    private static final Integer PORT = 18080;
    private static final String IP = "127.0.0.1";
    private byte[] bytes = new byte[1024];

    public void start() throws IOException {
        Socket socket = new Socket(IP,PORT);
        String toServer = "hello this is bio client";
        socket.getOutputStream().write(toServer.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        System.out.println("Client send hello");

        int len = socket.getInputStream().read(bytes);

        //输出
        System.out.println("Client receive " + new String(bytes, 0, len, StandardCharsets.UTF_8));
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        BioClient bioClient = new BioClient();
        bioClient.start();
    }
}
