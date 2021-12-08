package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * @author flowscolors
 * @date 2021-12-08 1:37
 */
public class NettyServer {
    private static final String IP = "127.0.0.1";
    private static final Integer PORT = 9999;


    public void start() throws InterruptedException {
        InetSocketAddress address = new InetSocketAddress(IP,PORT);
        EventLoopGroup group = new NioEventLoopGroup();
        final NettyServerHandler serverHandler = new NettyServerHandler();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(address)
                         /*
                        使用了一个特殊的类——ChannelInitializer。当一个新的连接
                        被接受时，一个新的子 Channel 将会被创建，而 ChannelInitializer 将会把一个
                        EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中。
                        ChannelHandler 将会收到有关入站消息的通知。
                        */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                                      @Override
                                      protected void initChannel(SocketChannel socketChannel) throws Exception {
                                          socketChannel.pipeline().addLast(serverHandler);
                                      }
                                  }
                    );
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully().sync();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        NettyServer nettyServer= new NettyServer();
        nettyServer.start();
    }
}
