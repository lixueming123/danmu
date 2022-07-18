package com.lxm.danmu.service.impl;

import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.mapper.MsgMapper;
import com.lxm.danmu.service.MsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@Service
public class MsgServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgService {


    @Override
    public boolean saveMsg(Msg msg, User user) {
        msg.setUid(user.getUid());
        return save(msg);
    }
}
