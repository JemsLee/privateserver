package com.pim.server.netty;

import com.pim.server.constants.CommParameters;
import com.pim.server.events.CommEvent;
import com.pim.server.message.MessageService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.springframework.stereotype.Component;


@Component
@ChannelHandler.Sharable
public class PrivateWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof WebSocketFrame) {
                handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String fromUid = PrivateChannelSupervise.getUserId(ctx.channel());
        if(fromUid != null){
            CommEvent.clearLocalUserInfo(fromUid);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String fromUid = PrivateChannelSupervise.getUserId(ctx.channel());
        if(fromUid != null){
            CommEvent.clearLocalUserInfo(fromUid);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String fromUid = PrivateChannelSupervise.getUserId(ctx.channel());
        if(fromUid != null){
            CommEvent.clearLocalUserInfo(fromUid);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        try {
            ctx.flush();
        } catch (Exception exception) {
        }

    }


    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        try {
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(
                        new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format(
                        "%s frame types not supported", frame.getClass().getName()));
            }

            String clientMessage = ((TextWebSocketFrame) frame).text();
            MessageService messageService = new MessageService();
            messageService.clientMessage = clientMessage;
            messageService.channel = ctx.channel();
            CommParameters.instance().getExecutor().execute(messageService);


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

}
