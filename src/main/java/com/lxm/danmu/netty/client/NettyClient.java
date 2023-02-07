package com.lxm.danmu.netty.client;

import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.netty.handler.WebSocketClientHandler;
import com.lxm.danmu.netty.proto.ChatMessage;
import io.netty.bootstrap.Bootstrap;
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
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public final class NettyClient {

    public static ChatMessage.request buildRequest(Msg msg) {
        ChatMessage.request.Builder builder = ChatMessage.request.newBuilder();
        builder.setSize(msg.getSize());
        builder.setContent(msg.getContent());
        builder.setColor(msg.getColor());
        builder.setBold(msg.getBold());
        builder.setItalic(msg.getItalic());
        builder.setPosition(msg.getPosition());
        builder.setName(msg.getName());
        builder.setTime(msg.getTime().toString());
        builder.setAvailable(msg.getAvailable() ? 1 : 0);
        return builder.build();
    }

    private ProtobufDecoder protobufDecoder = new ProtobufDecoder(ChatMessage.request.getDefaultInstance());
    private ProtobufEncoder protobufEncoder = new ProtobufEncoder();

    private Channel ch;

    public Channel getChannel() {
        return ch;
    }

    public void writeMsg(List<Msg> msgs) {
        Map<Long, List<Msg>> roomMsgMap = msgs.stream().collect(Collectors.groupingBy(Msg::getRid));
        for (Map.Entry<Long, List<Msg>> entry : roomMsgMap.entrySet()) {
            List<ChatMessage.request> responses = new ArrayList<>();
            List<Msg> messages = entry.getValue();
            for (Msg msg: messages) {
                ChatMessage.request response = buildRequest(msg);
                responses.add(response);
            }
            ch.writeAndFlush(responses);
        }
    }

    @PostConstruct
    public void init() {
        try {
            connect(URL, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static final String URL = "ws://127.0.0.1:8888/0/groupchat";

    public void connect(String URL, ChannelGroup clients, ChannelHandlerContext ctx) throws Exception {
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
                    p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler, protobufDecoder, protobufEncoder);
                }
            });

            ch = b.connect(uri.getHost(), port).sync().channel();
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
//                        ChatMessage.response response = buildResponse()
//                        byte[] bytes = response.toByteArray();
//                        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
//                        WebSocketFrame frame = new BinaryWebSocketFrame(buf);
//                        buf.retain();
//                        ch.writeAndFlush(frame);
                        TimeUnit.MILLISECONDS.sleep(200);
                    }
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}