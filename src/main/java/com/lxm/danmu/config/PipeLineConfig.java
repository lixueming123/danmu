/*
 * @author Zhanghh
 * @date 2019/4/23
 */
package com.lxm.danmu.config;

import com.lxm.danmu.handler.*;
import com.lxm.danmu.proto.ChatMessage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipeLineConfig extends ChannelInitializer<SocketChannel> {

    private static final int maxContentLength = 65536;

    @Autowired
    private ProtobufDecoder protobufDecoder;

    @Autowired
    private ProtobufEncoder protobufEncoder;

    @Autowired
    private HandShakeHandler handshakeHandler;

    @Autowired
    private FrameToByteHandler frameToByteHandler;

    @Autowired
    private ByteToFrameHandler byteToFrameHandler;

    @Autowired
    private ChatHandler chatHandler;

    @Autowired
    private LoggingHandler loggingHandler;

    @Autowired
    private ExceptionHandler exceptionHandler;

    @Bean
    public LoggingHandler loggingHandler() {
        return new LoggingHandler();
    }


    @Bean
    public ProtobufDecoder protobufDecoder() {
        return new ProtobufDecoder(ChatMessage.request.getDefaultInstance());
    }

    @Bean
    public ProtobufEncoder protobufEncoder() {
        return new ProtobufEncoder();
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline()
//                .addLast(loggingHandler)
                // HTTP请求的解码和编码
                .addLast(new HttpServerCodec())
                // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse，
                .addLast(new HttpObjectAggregator(maxContentLength))
                //用于处理接入的连接
                .addLast(handshakeHandler)
                .addLast(frameToByteHandler)
                .addLast(protobufDecoder)
                .addLast(byteToFrameHandler)
                .addLast(protobufEncoder)
                .addLast(chatHandler)
                .addLast(exceptionHandler);
    }
}
