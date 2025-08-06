package com.example.momo.domain.auth.slack;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.slack.api.Slack;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * momo-monitoring 채팅방에 Slack 메시지를 보냅니다.
 */

@Component
@Slf4j
public class SlackNotifier {

	private final Slack slack = Slack.getInstance();

	@Value("${notification.slack.webhook.url}")
	private String webhookUrl;

	/**
	 * 메시지 Consume(처리) 실패를 Slack으로 알리는 알림을 전송합니다
	 * @param className 실패가 발생한 소비자 클래스명
	 * @param eventType 처리에 실패한 이벤트 타입
	 * @param queueName 실패한 메시지가 전달된 큐 이름
	 * @param e 실패의 원인이 된 예외 객체
	 */
	@Retryable(
		retryFor = Exception.class,
		maxAttempts = 5,
		backoff = @Backoff(delay = 300, multiplier = 2)
	)
	public void notifyMessageConsumeFailure(String className,String eventType, String queueName, Exception e){
		try {
			// slack 메시지를 보낼 페이로드 생성
			Payload payload = Payload.builder()
				.blocks(buildEventFailureBlock(className, eventType, queueName, "메시지 처리 실패", e))
				.build();

			// slack 메시지 전송
			WebhookResponse response = slack.send(webhookUrl, payload);

			// 응답 http 메서드 확인
			if (response.getCode() == 200) {
				log.info("Slack 메시지 전송");
				return;
			}
			// 메시지 전송이 실패하면 런타임 예외
			throw new RuntimeException("Slack 메시지 전송 실패 response=" + response);

		} catch (IOException ioException) {
			throw new RuntimeException("Slack 메시지 전송 중 IOException", ioException);
		}
	}

	public List<LayoutBlock> buildEventFailureBlock(String className,String eventType, String queueName, String message, Exception e) {
		String errorMessage = e.getMessage();
		String errorName = e.getClass().getName();
		return List.of(
			header(h -> h.text(plainText("🚨[" + className + "] " + message))),
			divider(),
			section(s -> s.fields(List.of(
				markdownText("*EventType:* `" + eventType + "`"),
				markdownText("*Class Location:* `" + className + "`"),
				markdownText("*Queue:* `" + queueName + "`")
			))),
			section(s -> s.text(markdownText("*Error:*\n```" + errorName + ": " + errorMessage + "```"))),
			context(ctx -> ctx.elements(List.of(
				markdownText("⏱ " + Instant.now().toString())
			))),
			section(s -> s.text(markdownText("\u200B")))
		);
	}


	@Recover
	public void recover(Exception retryEx,String message) {
		// Retryable 의 최대 횟수를 모두 실패하면 ELK에서 모니터링 할 수 있도록 error 로그를 남김.
		log.error("Slack 메시지 전송 실패 : text={}", message,retryEx);
	}
}
