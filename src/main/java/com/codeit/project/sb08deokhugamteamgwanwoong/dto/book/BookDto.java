package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import lombok.Builder;

@Builder
public record BookDto(
    String id,
    String title,
    String author,
    String description,
    String publisher,
    String publishedDate,
    String isbn,
    String thumbnailUrl,
    Integer reviewCount,
    Double rating,
    String createdAt,
    String updatedAt) {
}
