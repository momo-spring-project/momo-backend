package com.example.momo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MomoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MomoApplication.class, args);
	}

}
