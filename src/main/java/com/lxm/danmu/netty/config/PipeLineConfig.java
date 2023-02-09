
package com.lxm.danmu.netty.config;

import com.lxm.danmu.netty.client.ExtProtobufDecoder;
import com.lxm.danmu.netty.client.ExtProtobufEncoder;
import com.lxm.danmu.netty.handler.*;
import com.lxm.danmu.netty.proto.ChatMessage;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
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

    @Autowired
    private ExtProtobufEncoder extProtobufEncoder;

    @Autowired
    private ExtProtobufDecoder extProtobufDecoder;

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

    @Bean
    public ExtProtobufDecoder extProtobufDecoder() {
        return new ExtProtobufDecoder(ChatMessage.request.getDefaultInstance());
    }

    @Bean
    public ExtProtobufEncoder extProtobufEncoder() {
        return new ExtProtobufEncoder();
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
                .addLast(byteToFrameHandler)
//                .addLast(protobufDecoder)
//                .addLast(protobufEncoder)
                .addLast(extProtobufDecoder)
                .addLast(extProtobufEncoder)
                .addLast(chatHandler)
                .addLast(exceptionHandler);
    }
}
