package com.lxm.danmu.controller;


import com.lxm.danmu.config.RequireAuthenticate;
import com.lxm.danmu.config.UserContext;
import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.kafka.HttpReqDmMsgProducer;
import com.lxm.danmu.rabbitmq.MqSender;
import com.lxm.danmu.common.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/message")
public class MsgController {

//    @Autowired
//    private MqSender mqSender;
    @Autowired
    HttpReqDmMsgProducer producer;

    @PostMapping("/save")
    @RequireAuthenticate
    public RespBean save(@RequestBody Msg msg) {
        User user = UserContext.getUser();
        msg.setUid(user.getUid());
//        mqSender.sendMessage(msg);
        producer.asyncSend(msg);
        return RespBean.success();
    }

}
