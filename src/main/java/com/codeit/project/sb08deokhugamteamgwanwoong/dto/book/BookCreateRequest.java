package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record BookCreateRequest(
    @NotBlank
    String title,
    @NotBlank
    String author,
    @NotBlank
    String description,
    @NotBlank
    String publisher,
    @NotNull
    LocalDate publishedDate,
    String isbn
) {
}
