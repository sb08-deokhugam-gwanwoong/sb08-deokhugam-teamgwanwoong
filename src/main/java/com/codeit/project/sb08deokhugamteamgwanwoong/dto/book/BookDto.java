package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import lombok.Builder;

@Builder
public record BookDto(
    String title,
    String author,
    String publisher,
    String isbn,
    String description,
    String imageUrl
) {
}
