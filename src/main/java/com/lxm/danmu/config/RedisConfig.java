package com.lxm.danmu.config;

import com.lxm.danmu.netty.session.LiveSession;
import com.lxm.danmu.netty.session.LiveSessionMemoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // val序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // hash key 序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // hash value 序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // 设置连接工厂
        redisTemplate.setConnectionFactory(factory);
        return redisTemplate;
    }

    @Bean
    public LiveSession liveSession() {
        return new LiveSessionMemoryImpl();
    }

}