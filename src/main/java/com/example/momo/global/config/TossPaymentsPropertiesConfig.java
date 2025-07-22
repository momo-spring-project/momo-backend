package com.example.momo.global.config;

import com.example.momo.domain.payment.infra.toss.TossPaymentsConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentsConfig.class)
public class TossPaymentsPropertiesConfig {
}
