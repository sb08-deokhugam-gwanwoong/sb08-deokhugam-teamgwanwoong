package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.nickname", target = "userNickname")
  @Mapping(source = "review.id", target = "reviewId")
  CommentDto toDto(Comment comment);
}
