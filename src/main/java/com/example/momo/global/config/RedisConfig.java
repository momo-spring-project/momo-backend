package com.example.momo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

	@Value("${REDIS_HOST}")
	private String redisHost;

	@Value("${REDIS_PORT}")
	private int redisPort;

	@Bean
	@Primary
	public LettuceConnectionFactory redisConnectionFactory() {
		LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
		factory.setShareNativeConnection(false);
		return factory;
	}

	@Bean
	public RedisTemplate<String, MeetingReminderMessage> redisReminderTemplate(
		RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {

		ObjectMapper objectMapperCopy = objectMapper.copy();
		objectMapperCopy.registerModule(new JavaTimeModule());
		objectMapperCopy.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<MeetingReminderMessage> serializer =
			new Jackson2JsonRedisSerializer<>(objectMapperCopy, MeetingReminderMessage.class);

		RedisTemplate<String, MeetingReminderMessage> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		//Key/Value
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);

		// Hash
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(serializer);

		template.setEnableTransactionSupport(true);
		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public RedisTemplate<String, String> redisStringTemplate(RedisConnectionFactory cf) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(cf);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());

		template.setEnableTransactionSupport(true);
		template.afterPropertiesSet();
		return template;
	}
}
