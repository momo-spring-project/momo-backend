package com.example.momo.domain.auth.application.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.momo.global.common.dto.AuthUser;

public class CustomOAuth2User extends AuthUser implements OAuth2User {
	private final OAuth2Response oAuth2Response;

	public CustomOAuth2User(Long id, OAuth2Response oAuth2Response) {
		super(id);
		this.oAuth2Response = oAuth2Response;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return oAuth2Response.getAttribute();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collection = new ArrayList<>();

		collection.add(new GrantedAuthority() {

			@Override
			public String getAuthority() {

				return "ROLE_USER";
			}
		});

		return collection;
	}

	@Override
	public String getName() {
		return oAuth2Response.getNickname();
	}
}
