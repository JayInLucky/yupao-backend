package com.luo.yupao.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 自定义序列化
 * @author jj
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        //指定k-v的数据结构
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        指定redis连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);
//        指定序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

}
