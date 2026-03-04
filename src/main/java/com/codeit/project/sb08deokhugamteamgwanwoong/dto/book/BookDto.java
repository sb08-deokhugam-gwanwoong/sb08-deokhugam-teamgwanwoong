package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailUrl,
    Integer reviewCount,
    Double rating,
    Instant createdAt,
    Instant updatedAt) {
}
