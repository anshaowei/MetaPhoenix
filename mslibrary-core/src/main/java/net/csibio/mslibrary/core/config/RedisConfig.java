package net.csibio.mslibrary.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * 暂时未使用Redis,用于缓存Aird文件的时候性能较低
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisConfig {

//    @Bean
//    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory){
//        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setConnectionFactory(connectionFactory);
//        return redisTemplate;
//    }

    /**
     * RedisTemplate配置
     *
     * @param lettuceConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        log.info(" --- redis config init --- ");
        // Jackson 3 序列化：让所有字段可见，并通过 PolymorphicTypeValidator 重新启用默认类型化。
        // 下面的校验器较为宽松（允许 Object 的任意子类型），以保持与旧配置一致的行为；
        // 在生产环境真正启用 Redis 前，应将其收紧为 allowIfSubType(具体包名)，
        // 因为对不可信输入开启默认类型化存在反序列化安全风险。
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        ObjectMapper om = JsonMapper.builder()
                .changeDefaultVisibility(vc -> vc.with(JsonAutoDetect.Visibility.ANY))
                .activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL)
                .build();
        JacksonJsonRedisSerializer<Object> jacksonSerializer = new JacksonJsonRedisSerializer<>(om, Object.class);
        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);// key序列化
        redisTemplate.setValueSerializer(jacksonSerializer);// value序列化
        redisTemplate.setHashKeySerializer(stringSerializer);// Hash key序列化
        redisTemplate.setHashValueSerializer(jacksonSerializer);// Hash value序列化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
