package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

public interface BookMetadataProvider<T> {
    /**
     * ISBN 또는 검색어를 통해 도서 정보를 조회합니다.
     *
     * @param query 검색어 (ISBN, 제목 등)
     * @return 도서 정보 DTO
     */
    T getBookMetadata(String query);
}
