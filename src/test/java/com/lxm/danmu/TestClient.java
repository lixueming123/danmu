package com.lxm.danmu;

import com.lxm.danmu.netty.proto.ChatMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class TestClient {

    public static ChatMessage.response buildResponse(int size, String content,
                                               String color, boolean bold, boolean italic,
                                               int position, String name, String time, int available) {
        ChatMessage.response.Builder builder = ChatMessage.response.newBuilder();
        builder.setSize(size);
        builder.setContent(content);
        builder.setColor(color);
        builder.setBold(bold);
        builder.setItalic(italic);
        builder.setPosition(position);
        builder.setName(name);
        builder.setTime(time);
        builder.setAvailable(available);
        return builder.build();
    }

    static final String URL = "ws://127.0.0.1:7000/1/groupchat";

    public static void connect(String URL, ChannelGroup clients, ChannelHandlerContext ctx) throws Exception {
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final WebSocketClientHandler handler = new WebSocketClientHandler(ctx, WebSocketClientHandshakerFactory
                    .newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    if (sslCtx != null) {
                        p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                    }
                    p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
                }
            });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();


            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                }
                else if ("bye".equals(msg.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                }
                else if ("ping".equals(msg.toLowerCase())) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
                    ch.writeAndFlush(frame);
                }
                else if ("send".equals(msg.toLowerCase())) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    String[] colors = {"#00", "yellow", "red", "blue", "green"};

                    for (int i = 0; i < 300; i++) {
                        Date date = new Date();
                        String format = sdf.format(date);
                        ChatMessage.response response = buildResponse(25, "哈哈" + i, colors[i % 5],
                                i % 2 == 0, i % 10 == 0, 0,
                                "test", format, 1);
                        byte[] bytes = response.toByteArray();
                        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
                        WebSocketFrame frame = new BinaryWebSocketFrame(buf);
                        buf.retain();
                        ch.writeAndFlush(frame);
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        connect(URL,null,null);
    }
}