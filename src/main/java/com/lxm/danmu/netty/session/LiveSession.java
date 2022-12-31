package com.lxm.danmu.netty.session;

import com.lxm.danmu.entity.Room;
import io.netty.channel.Channel;

import java.util.Set;

public interface LiveSession {
    /**
     * 创建一个房间
     * @return live
     */
    Live newLive(Room room);

    /**
     * 加入一个成员
     * @return live
     */

    Live joinMember(Long rid, Channel member);

    /**
     * 删除一个成员
     * @return live
     */
    Live removeMember(Long rid, Channel member);

    /**
     *  删除该房间
     * @return remove true or false
     */
    boolean removeLive(Long rid);

    /**
     * 获取组成员
     * @param rid live id
     * @return 成员集合, 没有成员会返回 empty set
     */
    Set<Channel> getMembers(Long rid);

}
