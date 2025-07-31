package com.example.momo.global.rabbitMQ;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestController {

	private final MessageProducer producer;

	@PostMapping("/send")
	public String sendMessage(@RequestBody String message) {
		producer.send(message);
		return "보냄";
	}
}
