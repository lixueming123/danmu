package com.lxm.danmu.service;

import com.lxm.danmu.entity.Msg;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxm.danmu.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
public interface MsgService extends IService<Msg> {
    /**
     * 保存弹幕消息
     */

    boolean saveMsg(Msg msg, User user);
}
