package com.lxm.danmu;

import com.lxm.danmu.netty.proto.ChatMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    public static final ProtobufDecoder decoder = new ProtobufDecoder(ChatMessage.response.getDefaultInstance());

    private Channel outboundChannel;
    private ChannelHandlerContext channel;
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;


    public WebSocketClientHandler(ChannelHandlerContext channel, WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
        this.channel = channel;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("handlerAdded");
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channelActive");
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("WebSocket Client 连接断开!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead0");
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);

            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus()
                    + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame fr = (BinaryWebSocketFrame) frame;
            // resposnse(ctx, frame);
            ByteBuf content = fr.content();

//            channel.writeAndFlush(textFrame.text());
            System.out.println("WebSocket Client received message: " + content);
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

}