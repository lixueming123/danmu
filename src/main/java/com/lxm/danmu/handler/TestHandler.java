package com.lxm.danmu.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Component;

@Component
@Sharable
public class TestHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(ctx.channel().attr(AttributeKey.valueOf("user")).get());
        System.out.println(ctx.channel().attr(AttributeKey.valueOf("rid")).get());
        System.out.println(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().attr(AttributeKey.valueOf("user")).get());
        System.out.println(ctx.channel().attr(AttributeKey.valueOf("rid")).get());

    }
}
