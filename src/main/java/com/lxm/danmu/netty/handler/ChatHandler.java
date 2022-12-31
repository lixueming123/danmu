package com.lxm.danmu.netty.handler;

import com.lxm.danmu.entity.User;
import com.lxm.danmu.netty.proto.ChatMessage;
import com.lxm.danmu.netty.session.LiveSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Sharable
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<ChatMessage.request> {
    public static final Map<Channel, User> userMap = new ConcurrentHashMap<>();

    @Autowired
    private LiveSession liveSession;

    private ChatMessage.response buildResponse(int size, String content,
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

    /**
     * WebSocket握手成功时被调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        Long rid = (Long) channel.attr(AttributeKey.valueOf("rid")).get();
        User user = (User) channel.attr(AttributeKey.valueOf("user")).get();
        if (rid != null) {
            liveSession.joinMember(rid, channel);
        }
        if (user != null) userMap.put(channel, user);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessage.request msg) throws Exception {
        Channel channel = ctx.channel();
        User user = userMap.get(channel);

//        if (user == null) {
//            return;
//        }

        Long rid = (Long) channel.attr(AttributeKey.valueOf("rid")).get();
        Set<Channel> members = liveSession.getMembers(rid);
        ChatMessage.response response = buildResponse(msg.getSize(), msg.getContent(), msg.getColor(), msg.getBold(),
                msg.getItalic(), msg.getPosition(), msg.getName(), msg.getTime(), msg.getAvailable());

        for (Channel member : members) {
            if (member != channel) {
                member.writeAndFlush(response);
            }
        }
    }

    /**
     * Channel不活跃时调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        Long rid = (Long) channel.attr(AttributeKey.valueOf("rid")).get();
        if (rid != null) {
            userMap.remove(channel);
            liveSession.removeMember(rid, channel);
            log.error("channel: {}被删除,房间号码:{},当前人数为:{}", channel.id().asShortText(), rid, liveSession.getMembers(rid).size());
        }
    }
}
