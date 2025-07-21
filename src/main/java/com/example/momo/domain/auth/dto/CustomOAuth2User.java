package com.example.momo.domain.auth.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User extends AuthUser implements OAuth2User {

	public CustomOAuth2User(Long id) {
		super(id);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Map.of();
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
		return "";
	}
}
