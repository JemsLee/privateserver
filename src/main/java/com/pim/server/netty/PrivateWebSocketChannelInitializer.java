package com.pim.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PrivateWebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private PrivateWebSocketHandler privateWebSocketHandler;

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast("logging", new LoggingHandler(LogLevel.DEBUG));
        ch.pipeline().addLast("http-codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536 * 5));
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/"));
        ch.pipeline().addLast("handler", privateWebSocketHandler);
        ch.pipeline().addLast(new IdleStateHandler(30, 30, 35, TimeUnit.SECONDS));
        ch.pipeline().addLast(new PrivateServiceHandler());
    }
}

