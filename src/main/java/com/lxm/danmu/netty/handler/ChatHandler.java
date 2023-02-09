package com.lxm.danmu.netty.handler;

import com.lxm.danmu.entity.Msg;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Sharable
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<List<ChatMessage.request>> {
    public static final Map<Channel, User> userMap = new ConcurrentHashMap<>();

    @Autowired
    private LiveSession liveSession;

    public static ChatMessage.response buildResponse(ChatMessage.request msg) {
        ChatMessage.response.Builder builder = ChatMessage.response.newBuilder();
        builder.setSize(msg.getSize());
        builder.setContent(msg.getContent());
        builder.setColor(msg.getColor());
        builder.setBold(msg.getBold());
        builder.setItalic(msg.getItalic());
        builder.setPosition(msg.getPosition());
        builder.setName(msg.getName());
        builder.setTime(msg.getTime().toString());
        builder.setAvailable(msg.getAvailable());
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
    protected void channelRead0(ChannelHandlerContext ctx, List<ChatMessage.request> msgs) throws Exception {
        log.info("chat message:{}", msgs);
        Channel channel = ctx.channel();
//        User user = userMap.get(channel);

//        if (user == null) {
//            return;
//        }

//        Long rid = (Long) channel.attr(AttributeKey.valueOf("rid")).get();
        Long rid = (long) msgs.get(0).getRid();
        Set<Channel> members = liveSession.getMembers(rid);
        List<ChatMessage.response> responses = new ArrayList<>();
        for (ChatMessage.request msg : msgs) {
            ChatMessage.response response = buildResponse(msg);
            responses.add(response);

        }
        for (Channel member : members) {
            if (member != channel) {
                member.writeAndFlush(responses);
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
