package com.example.momo;

import com.example.momo.domain.payments.infra.toss.TossPaymentsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
//@EnableJpaAuditing
@EnableConfigurationProperties(TossPaymentsConfig.class)
public class MomoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MomoApplication.class, args);
  }

}
