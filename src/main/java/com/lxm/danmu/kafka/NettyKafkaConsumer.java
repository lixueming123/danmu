package com.lxm.danmu.kafka;

import com.lxm.danmu.mapper.MsgMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
@Slf4j
public class NettyKafkaConsumer {
    @Resource
    MsgMapper mapper;
    @KafkaListener(topics = "dm_topic", groupId = "netty")
    public void consume(List<ConsumerRecord<?,String>> records) {
        log.info("收到的消息：{}", records);
    }
}
