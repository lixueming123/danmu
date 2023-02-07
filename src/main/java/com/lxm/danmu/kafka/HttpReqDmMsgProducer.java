package com.lxm.danmu.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxm.danmu.entity.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class HttpReqDmMsgProducer {
    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;
    public void asyncSend(Msg msg) {
        ListenableFuture<SendResult<String, String>> future = null;
        try {
            future = kafkaTemplate.send("dm_topic", objectMapper.writeValueAsString(msg));
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onFailure(Throwable ex) {
                    log.error("发送失败:{}", ex.getMessage());
                }

                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("发送成功:{}", result);
                }
            });
        } catch (JsonProcessingException ex) {
            log.error("json解析异常:{}", ex.getMessage());
        }

    }
}
