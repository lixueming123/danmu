package com.lxm.danmu.handler;

import cn.hutool.http.ContentType;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.service.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Component
@Sharable
public class HandShakeHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 请求类型常量
     */
    private static final String WEBSOCKET_UPGRADE = "websocket";
    private static final String WEBSOCKET_HEAD = "Upgrade";

    /*
     * 握手工厂
     */
    private static final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
            null, null, false);

    @Autowired
    private UserService userService;

    @Value("${netty.context-path}")
    private String path;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 不自动执行
    }

    private User checkLogin(FullHttpRequest request) {
        try {
            if (request.headers() != null) {
                Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(request.headers().get("Cookie"));
                for (Cookie cookie : cookies) {
                    if ("userTicket".equals(cookie.name())) {
                        if (userService.loginCheck(cookie.value())) {
                            return userService.getUser(cookie.value());
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("收到的消息:" + msg);
        // 处理WebSocket协议的信息
        if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
        // 处理HTTP协议升级为WebSocket
        else if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
    }

    /*
     * 完整发送完消息后关闭通道
     */
    private void closeFuture(ChannelHandlerContext ctx, Object msg) {
        ctx.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
    }

    /*
     * 发送400 Bad Request
     */
    private void sendBadRequest(ChannelHandlerContext ctx) {
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                CharsetUtil.UTF_8);
        res.content().writeBytes(buf);
        buf.release();
        HttpUtil.setContentLength(res, res.content().readableBytes());
        closeFuture(ctx, res);
    }

    private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest req) {

//        System.out.println("......");
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,
                req.decoderResult().isSuccess() ? OK : BAD_REQUEST);
        ByteBuf buf = Unpooled.copiedBuffer(resp.status().toString(),
                CharsetUtil.UTF_8);

        resp.content().writeBytes(buf);
        HttpUtil.setContentLength(resp, resp.content().readableBytes());
        HttpUtil.setKeepAlive(resp, keepAlive);
        buf.release();
        ctx.writeAndFlush(resp);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) {
        String[] split = req.uri().split("/");
        if (split.length < 3) {
            sendBadRequest(ctx);
            return;
        }
        // 如果HTTP解码失败，返回HTTP异常
        if (!req.decoderResult().isSuccess()
                || !WEBSOCKET_UPGRADE.equals(req.headers().get(WEBSOCKET_HEAD))
                || !path.equals( "/" + split[2])) {
            sendBadRequest(ctx);
            return;
        }
        WebSocketServerHandshaker webSocketServerHandshaker = wsFactory.newHandshaker(req);
        if (webSocketServerHandshaker != null) {
            webSocketServerHandshaker.handshake(ctx.channel(), req);
            // 设置UserInfo
            ctx.channel().attr(AttributeKey.valueOf("user"))
                    .set(checkLogin(req));
            ctx.channel().attr(AttributeKey.valueOf("rid"))
                    .set(Long.parseLong(split[1]));
            // 握手成功才添加到ChannelGroup
//            writeResponse(ctx, req);
            ctx.fireChannelActive();
        } else {
            sendBadRequest(ctx);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx,
                                      WebSocketFrame frame) {
        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            // 将其移出ChannelGroup
            closeFuture(ctx, frame.retain());
        }
        // 判断是否是Ping消息
        else if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(
                    new PongWebSocketFrame(frame.content().retain()));
        } else {
            ctx.fireChannelRead(frame.retain());
        }
    }

}
