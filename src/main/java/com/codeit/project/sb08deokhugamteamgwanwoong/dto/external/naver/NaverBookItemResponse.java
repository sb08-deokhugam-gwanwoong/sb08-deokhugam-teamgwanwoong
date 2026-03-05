package com.codeit.project.sb08deokhugamteamgwanwoong.dto.external.naver;

public record NaverBookItemResponse(
    String title,
    String author,
    String publisher,
    String isbn,
    String description,
    String image,
    String pubdate
) {
}
