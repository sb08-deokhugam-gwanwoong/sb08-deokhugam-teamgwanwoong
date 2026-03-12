package com.codeit.project.sb08deokhugamteamgwanwoong.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserPasswordUpdateRequest(
    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    String currentPassword,

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$")
    String newPassword
) {
}
