package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewLikeDto;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ReviewLikeMapper {

    ReviewLikeDto toDto(UUID reviewId, UUID userId, boolean liked);
}
