package com.example.momo.domain.auth.application;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.momo.domain.auth.domain.UserSocial;
import com.example.momo.domain.auth.domain.dto.CustomOAuth2User;
import com.example.momo.domain.auth.domain.dto.GoogleOAuth2Dto;
import com.example.momo.domain.auth.domain.dto.NaverOAuth2Dto;
import com.example.momo.domain.auth.domain.dto.OAuth2Response;
import com.example.momo.domain.auth.enums.OAuth2Type;
import com.example.momo.domain.auth.infra.UserSocialRepository;
import com.example.momo.domain.user.domain.User;
import com.example.momo.domain.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {
	private final UserSocialRepository userSocialRepository;
	private final UserRepository userRepository;

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
		Optional<User> optionalUser = userRepository.findByEmailAndIsDeletedFalse(oAuth2Response.getEmail());
		User user = null;

		// 연동된 계정도 없고 해당 이메일로 회원가입된 계정도 없다면 새로 생성
		if (userSocial == null && optionalUser.isEmpty()) {
			user = new User(
				oAuth2Response.getNickname(),
				oAuth2Response.getEmail(),
				null,
				null,
				0.0,
				0.0
			);
			userRepository.save(user);
			userSocialRepository.save(
				UserSocial.of(user, oAuth2Response.getProviderId(), OAuth2Type.fromName(registrationId)));
			log.info("계정({})을 생성하고 {} 계정을 연동합니다.", user.getEmail(), registrationId);

			// 연동된 계정은 없지만 해당 이메일로 회원가입 되어있을 때 자동 연동
		} else if (userSocial == null && !optionalUser.isEmpty()) {
			user = optionalUser.get();
			userSocialRepository.save(
				UserSocial.of(user, oAuth2Response.getProviderId(), OAuth2Type.fromName(registrationId)));
			log.info("계정({})에 {} 계정을 연동합니다.", user.getEmail(), registrationId);
			// 연동된 계정이 있을 때
		} else {
			user = userSocial.getUser();
			log.info("계정({})에 {} 계정으로 로그인합니다.", user.getEmail(), registrationId);
		}

		return new CustomOAuth2User(user.getId(), oAuth2Response);
	}
}
