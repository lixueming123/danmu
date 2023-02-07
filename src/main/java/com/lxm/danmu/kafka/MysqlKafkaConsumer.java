package com.lxm.danmu.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxm.danmu.entity.Msg;
import com.lxm.danmu.mapper.MsgMapper;
import com.lxm.danmu.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MysqlKafkaConsumer {

    @Resource
    MsgService service;
    @Autowired
    ObjectMapper objectMapper;
    @KafkaListener(topics = "dm_topic", groupId = "mysql")
    public void consume(List<ConsumerRecord<?,String>> records) {
        List<Msg> msgs = new ArrayList<>();
        log.info("收到的消息：{}", records);
        for (ConsumerRecord<?, String> record : records) {
            String content = record.value();
            try {
                Msg msg = objectMapper.readValue(content, Msg.class);
                msgs.add(msg);
            } catch (JsonProcessingException e) {
                log.error("json解析异常");
            }
        }
        service.saveBatch(msgs);
    }

}
