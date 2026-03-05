package com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
    @NotBlank(message = "수정할 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이내로 작성해주세요.")
    String content
) {
}
