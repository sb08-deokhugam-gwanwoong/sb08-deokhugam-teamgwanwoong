package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import lombok.Builder;

@Builder
public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    String publishedDate,
    String isbn,
    String thumbnailImage
) {

}
