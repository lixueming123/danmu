package com.lxm.danmu.session;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Live {

    public static final Live EMPTY_LIVE = new Live("empty", new ConcurrentHashSet<>());


    /**
     * 房间id
     */
    private Long rid;


    /**
     * 房间创建者id
     */
    private Long uid;

    /**
     * 房间创建者昵称
     */
    private String nickname;

    /**
     * 房间名称
     */
    private String name;


    /**
     * 房间成员(channel)
     */
    private Set<Channel> members;

    public Live(String name, Set<Channel> emptySet) {
        this.name = name;
        this.members = emptySet;
    }
}
