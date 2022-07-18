package com.lxm.danmu.rabbitmq;

import com.lxm.danmu.config.RabbitConfig;
import com.lxm.danmu.entity.Msg;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(Msg msg) {
        rabbitTemplate.convertAndSend(RabbitConfig.DM_EXCHANGE, "dm.message", msg);
    }

}
