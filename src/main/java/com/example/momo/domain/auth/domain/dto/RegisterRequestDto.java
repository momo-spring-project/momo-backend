package com.example.momo.domain.auth.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank
    private String nickname;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
}
