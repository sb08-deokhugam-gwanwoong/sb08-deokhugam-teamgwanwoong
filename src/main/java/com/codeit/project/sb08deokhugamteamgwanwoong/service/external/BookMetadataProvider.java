package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;

public interface BookMetadataProvider {
    /**
     * ISBN 또는 검색어를 통해 도서 정보를 조회합니다.
     *
     * @param query 검색어 (ISBN, 제목 등)
     * @return 도서 정보 DTO
     */
    BookDto getBookMetadata(String query);
}
