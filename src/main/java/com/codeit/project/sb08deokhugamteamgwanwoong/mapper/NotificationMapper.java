package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotificationMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "review.id", target = "reviewId")
  @Mapping(source = "isConfirmed", target = "confirmed")
  NotificationDto toDto(Notification notification);
}
