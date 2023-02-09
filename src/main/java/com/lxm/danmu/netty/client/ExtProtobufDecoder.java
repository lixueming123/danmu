package com.lxm.danmu.netty.client;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.internal.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

@ChannelHandler.Sharable
public class ExtProtobufDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final boolean HAS_PARSER;

    static {
        boolean hasParser = false;
        try {
            // MessageLite.getParserForType() is not available until protobuf 2.5.0.
            MessageLite.class.getDeclaredMethod("getParserForType");
            hasParser = true;
        } catch (Throwable t) {
            // Ignore
        }

        HAS_PARSER = hasParser;
    }

    private final MessageLite prototype;
    private final ExtensionRegistryLite extensionRegistry;

    /**
     * Creates a new instance.
     */
    public ExtProtobufDecoder(MessageLite prototype) {
        this(prototype, null);
    }

    public ExtProtobufDecoder(MessageLite prototype, ExtensionRegistry extensionRegistry) {
        this(prototype, (ExtensionRegistryLite) extensionRegistry);
    }

    public ExtProtobufDecoder(MessageLite prototype, ExtensionRegistryLite extensionRegistry) {
        this.prototype = ObjectUtil.checkNotNull(prototype, "prototype").getDefaultInstanceForType();
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] array;
        final int offset;
        final int length = msg.readableBytes();
        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = ByteBufUtil.getBytes(msg, msg.readerIndex(), length, false);
            offset = 0;
        }

        int size = msg.readShort();
        List<MessageLite> msgs = new ArrayList<>(size);
        int idx = offset + 2;
        for (int i = 0; i < size; i++) {
            int msgLength = msg.readShort();
            idx += 2;
            if (extensionRegistry == null) {
                if (HAS_PARSER) {
                    msgs.add(prototype.getParserForType().parseFrom(array, idx, msgLength));
                } else {
                    msgs.add(prototype.newBuilderForType().mergeFrom(array, idx, msgLength).build());
                }
            } else {
                if (HAS_PARSER) {
                    msgs.add(prototype.getParserForType().parseFrom(
                            array, idx, msgLength, extensionRegistry));
                } else {
                    msgs.add(prototype.newBuilderForType().mergeFrom(
                            array, idx, msgLength, extensionRegistry).build());
                }
            }
            idx += msgLength;
            msg.readerIndex(idx);
        }
        out.add(msgs);
    }
}
