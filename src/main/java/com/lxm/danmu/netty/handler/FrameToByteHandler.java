package com.lxm.danmu.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Sharable
public class FrameToByteHandler extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) {
        ByteBuf buf = frame.content();
        objs.add(buf);
        buf.retain();
    }
}
