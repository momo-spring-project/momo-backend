package com.example.momo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		LettuceConnectionFactory factory = new LettuceConnectionFactory();
		factory.setShareNativeConnection(false);
		return factory;
	}

	@Bean
	public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		template.setEnableTransactionSupport(true);
		template.afterPropertiesSet();
		return template;
	}

}
