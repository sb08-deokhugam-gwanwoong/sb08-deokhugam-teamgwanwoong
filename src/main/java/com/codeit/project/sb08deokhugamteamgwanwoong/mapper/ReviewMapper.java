package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // 기본 조회용
    @Mapping(source = "review.book.id", target = "bookId")
    @Mapping(source = "review.book.title", target = "bookTitle")
    @Mapping(source = "thumbnail", target = "bookThumbnailUrl")
    @Mapping(source = "review.user.id", target = "userId")
    @Mapping(source = "review.user.nickname", target = "userNickname")
    @Mapping(source = "review.createdAt", target="createdAt")
    @Mapping(source = "review.updatedAt", target="updatedAt")
    ReviewDto toDto(Review review, boolean likedByMe, String thumbnail);
}