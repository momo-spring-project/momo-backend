package com.example.momo.domain.payment.application.listener;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.momo.domain.meeting.domain.Meeting;
import com.example.momo.domain.meeting.domain.MeetingRepository;
import com.example.momo.domain.meeting.exception.MeetingException;
import com.example.momo.domain.meeting.exception.MeetingExceptionCode;
import com.example.momo.domain.payment.application.PaymentService;
import com.example.momo.domain.payment.domain.dto.CardPaymentTestRequestDto;
import com.example.momo.domain.payment.exception.PaymentErrorCode;
import com.example.momo.domain.payment.exception.PaymentException;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserRepository;
import com.example.momo.global.infrastructure.springEvent.MeetingEvents;
import com.example.momo.global.infrastructure.springEvent.meeting.RegisterEvents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

	private final MeetingRepository meetingRepository;
	private final UserRepository userRepository;
	private final PaymentService paymentService;
	private final ApplicationEventPublisher eventPublisher;

	/**
	 * 모임 등록 이벤트를 처리하여 결제를 진행합니다.
	 *
	 * @param event 모임 등록 이벤트
	 */
	@Async
	@EventListener
	@Transactional
	public void handleMeetingRegisterEvent(RegisterEvents event) {
		log.info("모임 등록 이벤트 수신 - meetingId: {}, userId: {}",
			event.meetingId(), event.userId());

		try {
			// 1. 모임 정보 조회
			Meeting meeting = meetingRepository.findById(event.meetingId())
				.orElseThrow(() -> new MeetingException(MeetingExceptionCode.MEETING_NOT_FOUND));

			// 2. 사용자 정보 조회
			User user = userRepository.findByIdAndIsDeletedFalse(event.userId())
				.orElseThrow(() -> new PaymentException(PaymentErrorCode.USER_NOT_FOUND));

			// 3. 중복 결제 확인은 PaymentServiceImpl에서 처리됨

			// 4. 참가비 확인
			int amount = meeting.getParticipationFee();

			// 5. 무료 모임인 경우
			if (amount == 0) {
				handleFreePayment(user, meeting);
				return;
			}

			// 6. 유료 모임인 경우 - 테스트 결제 진행
			handleTestPayment(user, meeting, amount);

		} catch (Exception e) {
			log.error("결제 처리 중 오류 발생 - meetingId: {}, userId: {}, error: {}",
				event.meetingId(), event.userId(), e.getMessage(), e);

			// 결제 실패 이벤트 발행 (필요시)
			eventPublisher.publishEvent(new MeetingEvents.ParticipationFailed(
				event.meetingId(), event.userId(), null));
		}
	}

	/**
	 * 무료 모임 결제 처리
	 * PaymentServiceImpl의 createFreePayment을 호출하여 처리
	 */
	private void handleFreePayment(User user, Meeting meeting) {
		// CardPaymentTestRequestDto 생성 (무료 모임용)
		CardPaymentTestRequestDto freeRequest = CardPaymentTestRequestDto.builder()
			.meetingId(meeting.getId())
			.build();

		// PaymentServiceImpl에서 처리 (PaymentCompletedEvent 발행 포함)
		paymentService.createTestKeyInPayment(freeRequest, user.getId());

		log.info("무료 모임 참가 처리 완료 - meetingId: {}, userId: {}",
			meeting.getId(), user.getId());
	}

	/**
	 * 테스트 유료 결제 처리
	 * 실제 운영 환경에서는 결제 창 URL을 반환하고,
	 * 사용자가 결제를 완료하면 콜백으로 처리해야 합니다.
	 */
	private void handleTestPayment(User user, Meeting meeting, int amount) {
		// 테스트 환경에서는 자동으로 결제 성공 처리
		// 실제로는 여기서 결제 URL을 생성하고 사용자에게 전달해야 함

		CardPaymentTestRequestDto testRequest = CardPaymentTestRequestDto.builder()
			.meetingId(meeting.getId())
			.cardNumber("4242424242424242") // 테스트 카드 번호
			.cardExpiry("12/25")
			.birth("880101")
			.build();

		try {
			// 테스트 결제 실행 - PaymentServiceImpl에서 PaymentCompletedEvent를 발행함
			paymentService.createTestKeyInPayment(testRequest, user.getId());

			log.info("테스트 결제 처리 완료 - meetingId: {}, userId: {}, amount: {}",
				meeting.getId(), user.getId(), amount);

		} catch (Exception e) {
			log.error("테스트 결제 처리 실패 - meetingId: {}, userId: {}, error: {}",
				meeting.getId(), user.getId(), e.getMessage());
			throw e;
		}
	}
}
