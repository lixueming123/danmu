package com.lxm.danmu.netty.session;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.lxm.danmu.entity.Room;
import com.lxm.danmu.service.RoomService;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LiveSessionMemoryImpl implements LiveSession {

    @PostConstruct
    public void init() {
        // 从数据库中读取房间信息放入liveMap中
        List<Room> list = roomService.list();
        for (Room room : list) {
            Live live = new Live();
            live.setRid(room.getId());
            live.setName(room.getRname());
            live.setNickname(room.getUsername());
            live.setMembers(new ConcurrentHashSet<>());
            live.setUid(room.getUid());
            liveMap.putIfAbsent(live.getRid(), live);
        }
    }

    // 存储房间信息
    public static final Map<Long, Live> liveMap = new ConcurrentHashMap<>();

    @Autowired
    RoomService roomService;

    @Override
    public Live newLive(Room room) {
        Live live = new Live();
        roomService.saveOrUpdate(room);

        live.setRid(room.getId());
        live.setName(room.getRname());
        live.setNickname(room.getUsername());
        live.setMembers(new ConcurrentHashSet<>());

        return liveMap.putIfAbsent(live.getRid(), live);
    }

    @Override
    public Live joinMember(Long rid, Channel member) {
        return liveMap.computeIfPresent(rid, (k, v) -> {
            v.getMembers().add(member);
            return v;
        });
    }

    @Override
    public Live removeMember(Long rid, Channel member) {
        return liveMap.computeIfPresent(rid, (k, v) -> {
            v.getMembers().remove(member);
            return v;
        });
    }

    @Override
    public boolean removeLive(Long rid) {
        return liveMap.remove(rid) != null;
    }

    @Override
    public Set<Channel> getMembers(Long rid) {
        return liveMap.getOrDefault(rid, Live.EMPTY_LIVE).getMembers();
    }

}
