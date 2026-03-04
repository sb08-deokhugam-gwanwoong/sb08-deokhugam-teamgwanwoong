package com.codeit.project.sb08deokhugamteamgwanwoong.dto.book;

import lombok.Builder;

@Builder
public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    // Naver Api가 날짜를 "20260304"같은 문자열러 반환하기 때문에 String으로 유지 -> 추후 서비스 계층에서 날짜 변환가능
    String publishedDate,
    String isbn,
    String thumbnailImage
) {

}
