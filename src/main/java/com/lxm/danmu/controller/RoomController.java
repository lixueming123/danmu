package com.lxm.danmu.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxm.danmu.config.RequireAuthenticate;
import com.lxm.danmu.config.UserContext;
import com.lxm.danmu.entity.Room;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.service.RoomService;
import com.lxm.danmu.netty.session.Live;
import com.lxm.danmu.netty.session.LiveSession;
import com.lxm.danmu.common.vo.RespBean;
import com.lxm.danmu.common.vo.RespBeanEnum;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private LiveSession liveSession;

    @Autowired
    private RoomService roomService;

    @GetMapping("/cnt/{rid}")
    public RespBean roomCount(@PathVariable("rid") Long rid) {
        return RespBean.success(liveSession.getMembers(rid).size());
    }

    @GetMapping("/list")
    public RespBean rooms(@RequestParam(value = "currentPage",defaultValue = "1") Integer currentPage) {
        Page<Room> page = new Page<>(currentPage,5);
        Page<Room> pageInfo = roomService.page(page, new QueryWrapper<Room>().orderByDesc("time"));
        return RespBean.success(pageInfo);
    }

    @GetMapping("/{rid}")
    public RespBean getRoom(@PathVariable("rid") Long rid) {
        Room room = roomService.getById(rid);
        return RespBean.success(room);
    }

    @GetMapping("/myLive")
    @RequireAuthenticate
    public RespBean getMyLive() {

        User user = UserContext.getUser();
        Long uid = user.getUid();
        QueryWrapper<Room> rq = new QueryWrapper<>();
        rq.eq("uid",uid).orderByDesc("time");
        List<Room> list = roomService.list(rq);
        return RespBean.success(list);
    }

    @PostMapping("/newLive")
    @RequireAuthenticate
    public RespBean newLive(@RequestBody Room room) {
        User user = UserContext.getUser();

        room.setUid(user.getUid());
        room.setTime(new Date());
        room.setUsername(user.getUsername());

        Live live = liveSession.newLive(room);
        if (live == null) {
            return RespBean.success(room);
        }
        return RespBean.error(RespBeanEnum.LIVE_EXIST);
    }

    @DeleteMapping("/remove/{rid}")
    @RequireAuthenticate
    public RespBean removeLive(@PathVariable("rid") Long rid) {
        // 房间不存在不可删除
        Room room = roomService.getById(rid);
        if (room == null) {
            return RespBean.error(RespBeanEnum.LIVE_NOT_EXIST);
        }

        // 不是本人创建的不可删除
        User user = UserContext.getUser();
        if (!room.getUid().equals(user.getUid())) {
            return RespBean.error(RespBeanEnum.LIVE_NO_AUTH);
        }

        // 房间内存在观众不可删除
        Set<Channel> members = liveSession.getMembers(rid);
        if (members.size() > 0) {
            return RespBean.error(RespBeanEnum.LIVE_HAS_MEMBER);
        }

        // 删除
        liveSession.removeLive(rid);
        roomService.removeById(rid);

        return RespBean.success();
    }
    
    @PostMapping("/modify")
    @RequireAuthenticate
    public RespBean modify(@RequestBody Room room) {
        User user = UserContext.getUser();
        Long uid = user.getUid();

        Room sr = roomService.getById(room.getId());

        if (!sr.getUid().equals(uid)) {
            return RespBean.error(RespBeanEnum.LIVE_NO_AUTH);
        }
        return RespBean.success(roomService.saveOrUpdate(room));
    }
}
