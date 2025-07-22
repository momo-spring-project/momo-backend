package com.example.momo.domain.auth.domain.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private Long id;
    private String email;
    private String nickname;

    public LoginResponseDto(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
