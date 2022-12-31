package com.lxm.danmu.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
@Sharable
public class ExceptionHandler extends ChannelDuplexHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
    }
}
