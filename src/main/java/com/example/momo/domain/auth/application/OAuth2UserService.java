package com.example.momo.domain.auth.application;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.momo.domain.auth.application.dto.CustomOAuth2User;
import com.example.momo.domain.auth.application.dto.GoogleOAuth2Dto;
import com.example.momo.domain.auth.application.dto.NaverOAuth2Dto;
import com.example.momo.domain.auth.application.dto.OAuth2Response;
import com.example.momo.domain.auth.domain.UserSocial;
import com.example.momo.domain.auth.enums.OAuth2Type;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.domain.user.application.dto.UserAuthResponseDto;
import com.example.momo.global.webclient.user.UserClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
	private final UserSocialRepository userSocialRepository;
	private final UserClient userClient;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		// 리소스 서버(네이버, 구글)에서 받아온 정보를 OAuth2User 객체에 담는다.
		OAuth2User oAuth2User = super.loadUser(userRequest);

		// 어떤 리소스 서버에서 왔는지 꺼냄 (naver or google)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// 각 리소스 서버에 맞는 객체 생성 (oAuthUser 객체와 User 엔티티 사이 매칭 역할)
		OAuth2Response oAuth2Response = null;
		if (registrationId.equals("naver"))
			oAuth2Response = new NaverOAuth2Dto(oAuth2User.getAttributes());
		else if (registrationId.equals("google"))
			oAuth2Response = new GoogleOAuth2Dto(oAuth2User.getAttributes());
		else
			return null;

		UserSocial userSocial = userSocialRepository.findByProviderId(oAuth2Response.getProviderId());
		UserAuthResponseDto userDto = userClient.getUserByEmailForAuth(oAuth2Response.getEmail());

		// 연동된 계정도 없고 해당 이메일로 회원가입된 계정도 없다면 새로 생성
		if (userSocial == null && userDto == null) {
			log.info("계정({})은 가입되어 있지 않은 SNS ID 입니다.", oAuth2Response.getEmail());
			throw new OAuth2AuthenticationException(new OAuth2Error("INVALID_SNS", "가입되어 있지 않은 SNS ID 입니다.", null));

			// 연동된 계정은 없지만 해당 이메일로 회원가입 되어있을 때 자동 연동
		} else if (userSocial == null) {

			userSocialRepository.save(
				UserSocial.of(userDto.id(), oAuth2Response.getProviderId(), OAuth2Type.fromName(registrationId)));
			log.info("계정({})에 {} 계정을 연동합니다.", userDto.email(), registrationId);
			// 연동된 계정이 있을 때
		} else {
			log.info("계정({})에 {} 계정으로 로그인합니다.", userDto.email(), registrationId);
		}

		return new CustomOAuth2User(userDto.id(), oAuth2Response);
	}
}
