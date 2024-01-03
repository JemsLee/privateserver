package com.pim.server.netty;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * WebSocke服务器起点
 * 版本：1.0.0
 * 2022-01-03
 * 作者：Jem.Lee
 */
@Component
public class PrivateWebSocketServer {

    @Autowired
    private PrivateWebSocketChannelInitializer privateWebSocketChannelInitializer;

    public NioEventLoopGroup boss;
    public NioEventLoopGroup work;


    public void init(int port) throws InterruptedException {

        System.out.println("Netty-based message server starting......");

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup work = new NioEventLoopGroup();
        this.boss = boss;
        this.work = work;

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, work);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(privateWebSocketChannelInitializer);
        Channel channel = bootstrap.bind(port).sync().channel();

        System.out.println("Netty-based message server started：" + channel);

    }


}

