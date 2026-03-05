package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank(message = "수정할 내용을 입력해주세요.")
    String content
) {
}
