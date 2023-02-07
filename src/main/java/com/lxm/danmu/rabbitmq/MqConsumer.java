package com.lxm.danmu.rabbitmq;

import com.lxm.danmu.config.RabbitConfig;
import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.mapper.MsgMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

//@Service
public class MqConsumer {

//    @Autowired
    private MsgMapper msgMapper;

//    @RabbitListener(queues = RabbitConfig.DM_QUEUE)
    public void consume(Message<Msg> msg) {
        Msg payload = msg.getPayload();
        msgMapper.insert(payload);
    }

}
