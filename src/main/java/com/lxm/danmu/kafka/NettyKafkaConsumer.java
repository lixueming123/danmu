package com.lxm.danmu.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.mapper.MsgMapper;
import com.lxm.danmu.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class NettyKafkaConsumer {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    NettyClient nettyClient;
    @KafkaListener(topics = "dm_topic", groupId = "netty")
    public void consume(List<ConsumerRecord<?,String>> records) {
        log.info("netty收到的消息：{}", records);
        List<Msg> msgs = new ArrayList<>();
        for (ConsumerRecord<?, String> record : records) {
            String content = record.value();
            try {
                Msg msg = objectMapper.readValue(content, Msg.class);
                msgs.add(msg);
            } catch (JsonProcessingException e) {
                log.error("json解析异常");
            }
        }
        nettyClient.writeMsg(msgs);
    }
}
