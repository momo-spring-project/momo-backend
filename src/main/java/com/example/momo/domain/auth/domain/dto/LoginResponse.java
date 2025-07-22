package com.example.momo.domain.auth.domain.dto;

import lombok.Getter;

@Getter
public class LoginResponse {
    private Long id;
    private String email;
    private String nickname;

    public LoginResponse(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
