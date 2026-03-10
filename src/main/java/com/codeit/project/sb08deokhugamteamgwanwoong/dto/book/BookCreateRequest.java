package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record BookCreateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    String title,
    @NotBlank(message = "저자는 필수입니다.")
    String author,
    @NotBlank(message = "설명은 필수입니다.")
    String description,
    @NotBlank(message = "출판사는 필수입니다.")
    String publisher,
    @NotNull(message = "출판일은 필수입니다.")
    LocalDate publishedDate,
    String isbn
) {
}
