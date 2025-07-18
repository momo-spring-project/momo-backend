package com.example.momo.domain.user.application;

import com.example.momo.domain.user.domain.dto.UserInfoResponseDto;

public interface UserService {

	UserInfoResponseDto getUserById(Long userId);

	UserInfoResponseDto getCurrentUser(Long CurrentUserId);

}
