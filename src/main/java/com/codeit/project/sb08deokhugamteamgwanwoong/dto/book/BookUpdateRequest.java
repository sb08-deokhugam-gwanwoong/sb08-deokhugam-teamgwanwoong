package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record BookUpdateRequest(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate
) {
}
