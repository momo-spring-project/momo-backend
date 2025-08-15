package com.example.momo.domain.messagehub.infra.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.momo.domain.messagehub.application.dto.MeetingReminderMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class MessageHubRedisScriptConfig {

	@Bean
	public DefaultRedisScript<Void> saveReminderScript() {
		DefaultRedisScript<Void> script = new DefaultRedisScript<>();
		script.setScriptText(
			"redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2]); " +
				"redis.call('HSET', KEYS[2], ARGV[2], ARGV[3]); "
		);
		script.setResultType(Void.class);
		return script;
	}

	@Bean
	public DefaultRedisScript<Void> deleteReminderScript() {
		DefaultRedisScript<Void> script = new DefaultRedisScript<>();
		script.setScriptText(
			"redis.call('ZREM', KEYS[1], unpack(ARGV)); " +
				"redis.call('HDEL', KEYS[2], unpack(ARGV));"
		);
		script.setResultType(Void.class);
		return script;
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
}
