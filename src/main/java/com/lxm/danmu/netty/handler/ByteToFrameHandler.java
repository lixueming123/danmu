package com.lxm.danmu.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Sharable
public class ByteToFrameHandler extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> objs) {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buf);
        objs.add(frame);
        frame.retain();
    }
}
