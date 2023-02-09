package com.lxm.danmu.netty.client;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.ObjectUtil;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

@ChannelHandler.Sharable
public class ExtProtobufEncoder extends MessageToMessageEncoder<List<MessageLiteOrBuilder>> {


    @Override
    protected void encode(ChannelHandlerContext ctx, List<MessageLiteOrBuilder> msgs, List<Object> out)
            throws Exception {
        int size = msgs.size();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeShort(size);
        for (MessageLiteOrBuilder msg : msgs) {
            byte[] bytes;
            if (msg instanceof MessageLite) {
                bytes = ((MessageLite) msg).toByteArray();
                buffer.writeShort(bytes.length);
                buffer.writeBytes(bytes);
            }
            if (msg instanceof MessageLite.Builder) {
                bytes = ((MessageLite.Builder) msg).build().toByteArray();
                buffer.writeShort(bytes.length);
                buffer.writeBytes(bytes);
            }
        }
        out.add(buffer);
    }
}

